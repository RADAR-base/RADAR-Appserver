# This script migrates all the notifications from the old XMPP server (https://github.com/RADAR-base/fcmxmppserverv2)
# to the new AppServer (https://github.com/RADAR-base/RADAR-Appserver).
# Requirements - Running HSQLDB instance holding data from XMPP server, Running instance of Management portal to query
# any missing properties and a Running instance of AppServer to add the data using REST endpoints.
import time

from clients import HsqlDBClient, AppServerClient, MpClient


def get_timezone(project: str, project_mappings) -> str:
    if project in project_mappings:
        return project_mappings[project]['timezone']
    elif "Default" in project_mappings:
        return project_mappings["Default"]['timezone']
    else:
        return "Europe/London"


def get_language(project: str, project_mappings) -> str:
    if project in project_mappings:
        return project_mappings[project]['language']
    elif "Default" in project_mappings:
        return project_mappings["Default"]['language']
    else:
        return "en"


def get_source_id(subject):
    for source in subject['sources']:
        if source['sourceTypeModel'] == "aRMT-App":
            return source['sourceId']
    return "unknown"


def sanitise_subject(subject: dict, project_mappings) -> dict:
    project = subject['project']['projectName']
    subject_cleaned = {
        'projectId': project,
        'enrolmentDate': subject['createdDate'],
        'timezone': get_timezone(project, project_mappings),
        'sourceId': get_source_id(subject),
        'lastOpened': subject['createdDate'],
        'language': get_language(project, project_mappings)
    }
    return subject_cleaned


def main():
    import argparse

    parser = argparse.ArgumentParser(description='Provide the values for various configs.',
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("--hsqldb-connection-url", type=str, default="jdbc:hsqldb:hsql://localhost:9001/status",
                        help="The JDBC connection URL for HSQLDB")
    parser.add_argument("--hsqldb-properties", type=dict, default={'user': "SA", 'password': ""},
                        help="HSQLDB connection properties as a dict.")
    parser.add_argument("--hsqldb-driver-path", type=str, default="./hsqldb.jar",
                        help="HSQLDB JDBC driver Jar file path.")

    parser.add_argument("--managementportal-base-url", type=str, default="http://localhost:8081",
                        help="Base URL where the Management Portal is exposed.")

    parser.add_argument("--managementportal-user", type=str, default="admin",
                        help="The user to authorise with using password grant code.")

    parser.add_argument("--managementportal-password", type=str, default="admin",
                        help="The password for the user to authorise with using password grant code.")

    parser.add_argument("--managementportal-client-id", type=str, default="ManagementPortalapp",
                        help="The client id of Management Portal frontend client for authorising with password grant code.")

    parser.add_argument("--managementportal-client-secret", type=str, default="",
                        help="The client secret of Management Portal frontend client for authorising with password grant code.")

    parser.add_argument("--appserver-base-url", type=str, default="http://localhost:8080",
                        help="Base URL where the Appserver is exposed.")

    parser.add_argument("--appserver-enable-auth", type=bool, default=False,
                        help="Whether to enable authorization for the appserver client.")

    parser.add_argument("--appserver-client-id", type=str, default="radar_appserver_client",
                        help="The client_id of the oauth client with access to res_AppServer resource and client_credentials grant code.")

    parser.add_argument("--appserver-client-secret", type=str, default="",
                        help="The client_secret of the oauth client with access to res_AppServer resource and client_credentials grant code.")

    parser.add_argument("--project-mapping-file", type=str, default="./project_mapping.json",
                        help="Path to the json file containing project mapping to timezone and language.")

    args = parser.parse_args()

    # Create clients for interaction with different services
    hsqldb_client = HsqlDBClient(args.hsqldb_connection_url,
                                 args.hsqldb_properties,
                                 args.hsqldb_driver_path,
                                 )
    mp_client = MpClient(args.managementportal_base_url,
                         user = args.managementportal_user,
                         password = args.managementportal_password,
                         client_id = args.managementportal_client_id,
                         client_secret = args.managementportal_client_secret)
    appserver_client = AppServerClient(args.appserver_base_url, args.appserver_enable_auth,
                                       args.managementportal_base_url, args.appserver_client_id,
                                       args.appserver_client_secret)

    import json
    try:
        with open(args.project_mapping_file, 'r') as f:
            project_mapping = json.load(f)
    except IOError as e:
        print(f"I/O Error when loading project mapping file. Using default.: {e}")
        project_mapping = {}
    except json.JSONDecodeError as e:
        print(f"Cannot load project mapping file. Make sure it is valid json. Using default.: {e}")
        project_mapping = {}

    # Read all notifications from HSQLDB
    notifs = hsqldb_client.get_all_notifications_from_hsqldb()

    notif_dict_list = dict()
    appserver_projects_created = dict()
    appserver_users_created = dict()

    # Create notification lists for each user
    for notif in notifs:

        # if scheduled time is before now, skip those notifications
        sch_time = float(notif[8]) / 1000
        if sch_time < time.time():
            print("Notification time is before now. Skipping...")
            continue

        # Create dict from tuple
        notif_dict = {
            'subjectId': notif[0],
            'fcmToken': notif[1],
            'notification_task_uuid': notif[2],
            'fcmMessageId': notif[3],
            'delivered': "false",
            'title': notif[5],
            'ttlSeconds': notif[6],
            'body': notif[7],
            'scheduledTime': sch_time,
            'type': "Unknown",
            'appPackage': "org.phidatalab.radar_armt",
            'sourceType': "aRMT-App"
        }

        # Create Project on AppServer using info from MP
        subject = sanitise_subject(mp_client.get_subject_details_from_mp(notif_dict["subjectId"]), project_mapping)
        project_dict = {'projectId': subject['projectId']}
        if project_dict['projectId'] not in appserver_projects_created.keys():
            project = appserver_client.create_project_on_appserver(project_dict)
            appserver_projects_created[project_dict['projectId']] = project
        else:
            project = appserver_projects_created[project_dict['projectId']]

        # Create Subject/User on AppServer using info from MP
        user_dict = {
            'projectId': subject['projectId'],
            'subjectId': notif_dict["subjectId"],
            'enrolmentDate': subject['enrolmentDate'],
            'timezone': subject['timezone'],
            'fcmToken': notif_dict['fcmToken'],
            'language': subject['language'],
        }
        if user_dict['subjectId'] not in appserver_users_created.keys():
            user = appserver_client.create_user_on_appserver(user_dict)
            appserver_users_created[user_dict['subjectId']] = subject
        else:
            user = appserver_users_created[user_dict['subjectId']]

        # Append notification to the subject's list of notifications
        notif_dict['projectId'] = subject['projectId']
        notif_dict['sourceId'] = subject['sourceId']
        if notif_dict['subjectId'] in notif_dict_list.keys():
            notif_dict_list[notif_dict['subjectId']].append(notif_dict)
        else:
            notif_dict_list[notif_dict['subjectId']] = [notif_dict]

    # prints number of users with notifications
    print(f"Number of users with valid notifications: {len(notif_dict_list)}")

    # Create notifications in batch
    appserver_client.write_batch_notifications_to_appserver(notif_dict_list)


if __name__ == "__main__":
    main()
