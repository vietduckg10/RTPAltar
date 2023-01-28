package com.ducvn.rtpaltar;

import net.minecraftforge.common.ForgeConfigSpec;

public class RTPAltarConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> search_for_surface;

    static {
        BUILDER.push("RTP Altar Config");

        search_for_surface = BUILDER.comment("Search for valid teleport surface if true, otherwise will teleport to maximum world height")
                .define("Search For Surface", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
