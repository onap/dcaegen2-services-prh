package org.onap.dcaegen2.services.prh.tasks;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.onap.dcaegen2.services.prh.exceptions.AAINotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/27/18
 */
@Configuration
public class DmaapCOnsumerTaskException {

    @Bean
    @Primary
    public DmaapConsumerTask registerSimpleDmaapConsumerTask() {
        DmaapConsumerTask dmaapConsumerTask = new DmaapConsumerTask();
        doThrow(new AAINotFoundException("Error")).when(dmaapConsumerTask).execute();
        return spy(dmaapConsumerTask);
    }
}
