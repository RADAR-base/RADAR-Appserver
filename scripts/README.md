# Scripts
A collection of scripts relevant to the AppServer

## migrate-xmpp-to-appserver.py

This script migrates all the notifications from the [old XMPP server](https://github.com/RADAR-base/fcmxmppserverv2)
to the [new AppServer](https://github.com/RADAR-base/RADAR-Appserver).

### Requirements
- Running HSQLDB instance holding data from XMPP server (Please stop the XMPP server before proceeding)
- Running instance of Management portal to query any missing properties
- Running instance of AppServer to add the data using REST endpoints.
- Libraries required by the script are written to [requirements.txt](requirements.txt)

### Usage
1. Install the dependencies using pip. It is recommended to use a virtual environment.
    ```shell script
    pip install -r requirements.txt
    ```
2. Run the script with `--help` or `-h` to display the help and usage options.
    ```shell script
    ➜ python3 migrate-xmpp-to-appserver.py --help
        usage: migrate-xmpp-to-appserver.py [-h]
                                            [--hsqldb-connection-url HSQLDB_CONNECTION_URL]
                                            [--hsqldb-properties HSQLDB_PROPERTIES]
                                            [--hsqldb-driver-path HSQLDB_DRIVER_PATH]
                                            [--managementportal-base-url MANAGEMENTPORTAL_BASE_URL]
                                            [--managementportal-user MANAGEMENTPORTAL_USER]
                                            [--managementportal-password MANAGEMENTPORTAL_PASSWORD]
                                            [--appserver-base-url APPSERVER_BASE_URL]
                                            [--appserver-enable-auth APPSERVER_ENABLE_AUTH]
                                            [--appserver-client-id APPSERVER_CLIENT_ID]
                                            [--appserver-client-secret APPSERVER_CLIENT_SECRET]
                                            [--project-mapping-file PROJECT_MAPPING_FILE]
        
        Provide the values for various configs.
        
        optional arguments:
          -h, --help            show this help message and exit
          --hsqldb-connection-url HSQLDB_CONNECTION_URL
                                The JDBC connection URL for HSQLDB (default:
                                jdbc:hsqldb:hsql://localhost:9001/status)
          --hsqldb-properties HSQLDB_PROPERTIES
                                HSQLDB connection properties as a dict. (default:
                                {'user': 'SA', 'password': ''})
          --hsqldb-driver-path HSQLDB_DRIVER_PATH
                                HSQLDB JDBC driver Jar file path. (default:
                                ./hsqldb.jar)
          --managementportal-base-url MANAGEMENTPORTAL_BASE_URL
                                Base URL where the Management Portal is exposed.
                                (default: http://localhost:8081)
          --managementportal-user MANAGEMENTPORTAL_USER
                                The user to authorise with using password grant code.
                                (default: admin)
          --managementportal-password MANAGEMENTPORTAL_PASSWORD
                                The password for the user to authorise with using
                                password grant code. (default: admin)
          --managementportal-secret MANAGEMENTPORTAL_SECRET
                                The client secret for ManagementPortalapp client for authorising with password grant code.
                                (default: )
          --appserver-base-url APPSERVER_BASE_URL
                                Base URL where the Appserver is exposed. (default:
                                http://localhost:8080)
          --appserver-enable-auth APPSERVER_ENABLE_AUTH
                                Whether to enable authorization for the appserver
                                client. (default: False)
          --appserver-client-id APPSERVER_CLIENT_ID
                                The client_id of the oauth client with access to
                                res_AppServer resource and client_credentials grant
                                code. (default: radar_appserver_client)
          --appserver-client-secret APPSERVER_CLIENT_SECRET
                                The client_secret of the oauth client with access to
                                res_AppServer resource and client_credentials grant
                                code. (default: )
          --project-mapping-file PROJECT_MAPPING_FILE
                                Path to the json file containing project mapping to
                                timezone and language. (default:
                                ./project_mapping.json)

    ```
3. Run the script by customising the options specified above according to your needs.