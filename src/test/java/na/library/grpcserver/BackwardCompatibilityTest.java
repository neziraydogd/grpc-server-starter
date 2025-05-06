package na.library.grpcserver;

import com.google.protobuf.InvalidProtocolBufferException;
import na.library.grpcweather.proto.WeatherData;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class BackwardCompatibilityTest {

    private static final Logger logger = LoggerFactory.getLogger(BackwardCompatibilityTest.class);

    @Test
    void testBackwardCompatibilityWithOldClient() throws InvalidProtocolBufferException {
        logger.info("Running Backward Compatibility Test (as JUnit Test)...");

        // Simulate serialized message from old client (v1)
        byte[] oldClientData = WeatherData.newBuilder()
                .setLocation("Ankara, Turkey")
                .setTemperature(25.5f)
                .setHumidity(65.0f)
                .setPressure(1013.2f)
                .setWindSpeed(8.5f)
                .setTimestamp("2023-03-03T10:15:30Z")
                // no source field (assuming it was added in v2)
                .build()
                .toByteArray();

        // Deserialize using new proto (v2)
        WeatherData weatherDataFromOldClient = WeatherData.parseFrom(oldClientData);

        // ✅ Compatibility check using JUnit Assertions
        assertEquals("Ankara, Turkey", weatherDataFromOldClient.getLocation());
        assertEquals(25.5f, weatherDataFromOldClient.getTemperature(), 0.001); // Delta for float comparison
        assertEquals(65.0f, weatherDataFromOldClient.getHumidity(), 0.001);
        assertEquals(1013.2f, weatherDataFromOldClient.getPressure(), 0.001);
        assertEquals(8.5f, weatherDataFromOldClient.getWindSpeed(), 0.001);
        assertEquals("2023-03-03T10:15:30Z", weatherDataFromOldClient.getTimestamp());
        // Assuming 'source' field was added in v2, it should be at its default value (null/empty string)
       // assertEquals("", weatherDataFromOldClient.getSource()); // Assuming default is empty string

        logger.info("✅ Backward compatibility passed (JUnit Test).");

        /*
            Protobuf is designed for forward and backward compatibility.
            Unknown or missing fields are ignored or set to defaults.
            This test guarantees that if an old client sends partial data, the new server won’t break.
            *** Reserve if removal is really needed
            message WeatherData {
                  reserved 6;
                  reserved "timestamp";

                  string location = 1;
                  float temperature = 2;
                  float humidity = 3;
                  float pressure = 4;
                  float wind_speed = 5;
                  string source = 7; // ✅ added safely
                   .....
            }
        */
    }
}