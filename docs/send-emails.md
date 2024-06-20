# Send emails feature

Appserver can send emails from the aRMT app on behalf of the study subject. Sending of emails is triggered by making a
POST message to the `/email/projects/{{projectId}}/users/{{subjectId}}` endpoint with a body that has the following
structure:

```json
{
  "to": "me@there.org",
  "cc": "you@there.org;andyou@there.org",
  "bcc": "everyone@there.org",
  "subject": "Hi!",
  "message": "How <b>you</b> doin ... !?"
}
```

The way how emails are sent is polymorphic. At present, the only method is by using the Firebase Trigger Email from
Firestore extension. This method involves placing a JSON document in the Firebase database collection. The extension is
configured to send emails from this database collection.

## Configuration of the Firebase Trigger Email from Firestore extension

To activate the email endpoint and configure Firebase emails set the following application properties in the
`application.properties` file:

```
send-email.enabled=true
send-email.type=firebase
```

To setup the _Using the Trigger Email extension_, log into your Firebase project console and follow the installation
instructions as described in the [extension docs](https://firebase.google.com/docs/extensions/official/firestore-send-email).