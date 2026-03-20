package mys.ticklib;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ticklib.MODID)
public class Ticklib {

    public static final String MODID = "ticklib";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Ticklib(FMLJavaModLoadingContext ctx) {
        var modEventBus = ctx.getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        LOGGER.info("Here is tickLib! tick..tock..!");
    }
}
