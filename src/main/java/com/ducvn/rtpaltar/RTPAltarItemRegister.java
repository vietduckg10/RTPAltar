package com.ducvn.rtpaltar;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RTPAltarItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RTPAltarMod.MODID);

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
