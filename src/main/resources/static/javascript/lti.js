function initialize() {

    // Verify that we're in the integration iframe
    if (!window.parent) {
        throw new Error('Not within iframe');
    }

    /* Add an event listener to listen for messages. When one is received, call onPostMessageReceived() */
    window.addEventListener("message", onPostMessageReceived, false);

    /* Post a message to tell Ultra we are here. */
    window.parent.postMessage({"type": "integration:hello"}, ultra_url + '/*');

}

/*
 * Called when we receive a message.
 */
function onPostMessageReceived(evt) {

    console.log('[lti] message from [' + evt.origin + ']');

    // Determine whether we trust the origin of the message (ultra). */
    const fromTrustedHost = evt.origin === ultra_url;

    if (!fromTrustedHost) {
        console.log('[lti] message from untrusted origin' + evt.origin);
        return;
    }

    // If Ultra is responding to our hello message
    if (evt.data.type === 'integration:hello') {
        console.log('[tol-uef] ULTRA says hello too');

        //Create a logged message channel so messages are logged to the Javascript console
        messagePort = new LoggedMessagePort(evt.ports[0], onMessageFromUltra);
        console.log('[tol-uef] message port created');

        // ULTRA is listening. Authorize ourselves using the access token
        messagePort.postMessage({
            type: 'authorization:authorize',
            token: access_token
        });
    }
}

function subscribeForEvents() {
    console.log('[tol-uef] subscribe for events ...');
    messagePort.postMessage({
        type: 'event:subscribe',
        subscriptions: ['portal:new', 'route'],
    });
}