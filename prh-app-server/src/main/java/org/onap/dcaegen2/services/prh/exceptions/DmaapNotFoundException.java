package org.onap.dcaegen2.services.prh.exceptions;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */
public class DmaapNotFoundException extends PrhTaskException {

    public DmaapNotFoundException(String message) {
        super(message);
    }
}
