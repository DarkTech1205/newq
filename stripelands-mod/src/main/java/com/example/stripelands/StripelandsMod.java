package com.example.stripelands;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StripelandsMod implements ModInitializer {
    public static final String MOD_ID = "stripelands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Stripelands] Float-precision coordinate truncation active. "
                + "Expect jitter and snapping starting at X/Z ±16,777,216. "
                + "World border hard-limit removed.");
    }
}
