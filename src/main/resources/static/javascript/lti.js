function initialize() {

    // Verify that we're in the integration iframe
    if (!window.parent) {
        throw new Error('Not within iframe');
    }

    /* Add an event listener to listen for messages. When one is received, call onPostMessageReceived() */
    window.addEventListener("message", onPostMessageReceived, false);
    sendCapabilitiesRequest();
}

/*
 * Called when we receive a message.
 */
function onPostMessageReceived(evt) {

    // Determine whether we trust the origin of the message (ultra). */
    const fromTrustedHost = evt.origin === 'https://ultra.t.edu.kuleuven.cloud';

    if (!fromTrustedHost) {
        console.log('[lti] message from untrusted origin:' + evt.origin);
        return;
    }

    console.log('[lti] message from [' + evt.origin + ']');

    if (evt.data.subject === 'lti.capabilities.response') {
        for (const msg of evt.data.supported_messages) {
          if (msg.subject === "lti.put_data") {
              let frameName = msg.frame;
              let targetFrame = frameName === "_parent" ? parent : parent.frames[frameName];
              let messageId = uniqueMessageId();
              sendStoreDataRequest(targetFrame,evt.origin,messageId,'state',state);
              sendStoreDataRequest(targetFrame,evt.origin,messageId,'nonce',nonce);
          }
        }
    }

}

function sendCapabilitiesRequest() {
    window.parent.postMessage({subject: 'lti.capabilities'}, '*');
}

function sendStoreDataRequest(frame, origin, messageId, key,value) {
    frame.postMessage({
        subject: 'lti.put_data',
        key: key + value,
        value: value,
        message_id: messageId
        },
        origin);
}

function uniqueMessageId() {
    return crypto.randomUUID();
}