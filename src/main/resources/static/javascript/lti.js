var messagePort;

function initialize() {

    // Verify that we're in the integration iframe
    if (!window.parent) {
        throw new Error('Not within iframe');
    }

    /* Add an event listener to listen for messages. When one is received, call onPostMessageReceived() */
    window.addEventListener("message", onPostMessageReceived, false);

}

/*
 * Called when we receive a message.
 */
function onPostMessageReceived(evt) {

    console.log('[lti] message from [' + evt.origin + ']');

    // Determine whether we trust the origin of the message (ultra). */
    const fromTrustedHost = evt.origin === 'ultra.t.edu.kuleuven.cloud';

    if (!fromTrustedHost) {
        console.log('[lti] message from untrusted origin' + evt.origin);
        return;
    }

}

function sendCapabilitiesRequest() {

    window.parent.postMessage({subject: 'lti.capabilities'}, '*');

}