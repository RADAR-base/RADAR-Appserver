import jaydebeapi
import requests
import time


class HsqlDBClient:
    SQL_QUERY = """
    select status_info.subject_id, status_info.fcm_token, status_info.notification_task_uuid,
     status_info.fcm_message_id, status_info.delivered, notification_info.title, notification_info.ttl_seconds,
     notification_info.message, notification_info.execution_time from notification_info inner join status_info on 
     notification_info.notification_task_uuid = status_info.notification_task_uuid where status_info.delivered = false
    """

    def __init__(self, connection_url, connection_properties, driver_path) -> None:
        super().__init__()
        print(f"URL: {connection_url}, Props: {connection_properties}")
        self.conn = jaydebeapi.connect("org.hsqldb.jdbcDriver",
                                       connection_url,
                                       connection_properties,
                                       driver_path,
                                       )

    def get_all_notifications_from_hsqldb(self) -> list:
        try:
            curs = self.conn.cursor()
            curs.execute(self.SQL_QUERY)
            return curs.fetchall()
        except self.conn.DatabaseError as e:
            print(f"Database Error occurred when accessing HSQLDB: {e}")
            exit(1)
        except self.conn.Error as e:
            print(f"Error: {e}")
            exit(1)


class MpClient:

    def __init__(self, mp_url, user="admin", password="admin", client_secret="", 
                 client_id="ManagementPortalapp") -> None:
        super().__init__()
        self.mp_url = mp_url
        token_url = f"{mp_url}/oauth/token"
        data = {
            'username': user,
            'password': password,
            'grant_type': "password"
        }
        with requests.post(token_url, auth=(client_id, client_secret), data=data) as response:
            if response.status_code == 200:
                print("Successful fetch token for Admin")
                self.token = response.json()["access_token"]
                self.auth_header = {'Authorization': f"Bearer {self.token}"}
            else:
                print(
                    f"Error: Failed to Fetch Token from MP - {response.url}, {response.status_code}, {response.content}, {response.headers}")
                exit(1)

        self.subject_cache = {}

    def get_subject_details_from_mp(self, subject_id: str) -> dict:
        subject_url = f"{self.mp_url}/api/subjects/{subject_id}"

        if subject_id in self.subject_cache.keys():
            return self.subject_cache.get(subject_id)

        with requests.get(subject_url, headers=self.auth_header) as response:
            try:
                if response.status_code == 200:
                    subject = response.json()
                    print(f"Queried subject from MP: {subject}")
                    self.subject_cache[subject_id] = subject
                    return subject
                else:
                    print(f"Error getting subject {subject_id} from MP: {response}")
                    exit(1)
            except ValueError as e:
                print(f"Error reading JSON response from MP: {e}")
                exit(1)


class AppServerClient:

    def __init__(self, appserver_url, enable_auth=False, mp_url="", client_id="radar_appserver_client",
                 client_secret="") -> None:
        super().__init__()
        self.appserver_url = appserver_url
        if enable_auth:
            token_url = f"{mp_url}/oauth/token?grant_type=client_credentials&client_id={client_id}&client_secret={client_secret}"
            with requests.post(token_url) as response:
                if response.status_code == 200:
                    self.token = response.json()["access_token"]
                    self.auth_header = {'Authorization': f"Bearer {self.token}"}
                else:
                    print(
                        f"Error getting token from MP for appserver client. {response.url}, {response.status_code}, {response.content}, {response.headers}")
                    exit(1)
        else:
            self.token = None
            self.auth_header = {}

    def create_project_on_appserver(self, project: dict) -> dict:
        project_endpoint = f"{self.appserver_url}/projects"
        with requests.post(project_endpoint, json=project, headers=self.auth_header) as response:
            if response.status_code == 201:
                print(f"Successful creation of project on appserver: {project}")
                return response.json()
            elif response.status_code == 417:
                print(f"The project {project} already exists")
                return requests.get(f"{project_endpoint}/{project['projectId']}").json()
            else:
                print(f"Error making post request to create project: {project}, {response}")
                exit(1)

    def create_user_on_appserver(self, user: dict) -> dict:
        user_endpoint = f"{self.appserver_url}/projects/{user['projectId']}/users"
        with requests.post(user_endpoint, json=user, headers=self.auth_header) as response:
            if response.status_code == 201:
                print(f"Successful creation of User on appserver: {user}")
                return response.json()
            elif response.status_code == 417:
                print(f"The user {user} already exists")
                return requests.get(f"{user_endpoint}/{user['subjectId']}").json()
            else:
                print(
                    f"Error making post request to create User: {user}, {response.status_code}, {response.url}, {response.content}, {response.headers}")
                exit(1)

    def write_notification_to_appserver(self, notification: dict) -> bool:
        notification_endpoint = f"{self.appserver_url}/projects/{notification['projectId']}/users/{notification['subjectId']}/messaging/notifications"
        # TODO
        raise NotImplementedError("To be implemented")

    def write_batch_notifications_to_appserver(self, notifications: dict) -> bool:
        flag = False
        for (user, notifs) in notifications.items():
            print(f"Creating notifications for {user} in project {notifs[0]['projectId']}")
            notifications_endpoint = f"{self.appserver_url}/projects/{notifs[0]['projectId']}/users/{user}/messaging/notifications/batch"
            notifications_dict = {'notifications': notifs}
            with requests.post(notifications_endpoint, json=notifications_dict, headers=self.auth_header) as response:
                if response.status_code == 200:
                    print(f"Successful write of notifications to AppServer for user: {user}")
                else:
                    print(
                        f"Error making post request to write notifications for user: {user}, {response.url}, {response.status_code}, {response.content}, {response.headers}")
                    flag = True
            # Add a delay to prevent overloading the server with too many batch requests at once
            time.sleep(1)

        if flag:
            return False
        else:
            return True
