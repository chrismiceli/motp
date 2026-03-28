import org.cry.otp.TOTP;
import org.junit.Assert;
import org.junit.Test;

public class TotpTests {
    @Test
    public void TotpRfc6238StandardTestSuite() {
        String[] seeds = {
                "3132333435363738393031323334353637383930",
                "3132333435363738393031323334353637383930313233343536373839303132",
                "31323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334"
        };

        long[] testTime = {
                59L, 1111111109L, 1111111111L,
                1234567890L, 2000000000L, 20000000000L
        };

        String[] expectedTotpValues = {
                "94287082", "46119246", "90693936", // Time: 59
                "07081804", "68084774", "25091201", // Time: 1111111109
                "14050471", "67062674", "99943326", // Time: 1111111111
                "89005924", "91819424", "93441116", // Time: 1234567890
                "69279037", "90698825", "38618901", // Time: 2000000000
                "65353130", "77737706", "47863826"  // Time: 20000000000
        };

        for (int i = 0; i < testTime.length; i += 3) {
            Assert.assertEquals(expectedTotpValues[i], TOTP.gen(seeds[0], testTime[i / 3], 8, 0, 30));
            Assert.assertEquals(expectedTotpValues[i + 1], TOTP.gen(seeds[1], testTime[i / 3], 8, 1, 30));
            Assert.assertEquals(expectedTotpValues[i + 2], TOTP.gen(seeds[2], testTime[i / 3], 8, 2, 30));
        }
    }
}