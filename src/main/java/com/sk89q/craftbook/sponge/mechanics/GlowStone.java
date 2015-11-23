package com.sk89q.craftbook.sponge.mechanics;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Module(moduleName = "GlowStone", onEnable="onInitialize", onDisable="onDisable")
public class GlowStone extends SpongeBlockMechanic {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    ConfigValue<BlockState> offBlock = new ConfigValue<>("off-block", "Sets the block that the glowstone turns into when turned off.", BlockTypes.SOUL_SAND.getDefaultState());

    @Override
    public void onInitialize() throws CraftBookException {
        //offBlock.load(config);
    }

    @Override
    public void onDisable() {
        //offBlock.save(config);
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {

        BlockSnapshot source;
        if(event.getCause().first(BlockSnapshot.class).isPresent())
            source = event.getCause().first(BlockSnapshot.class).get();
        else
            return;

        if(source.getState().getType() == BlockTypes.GLOWSTONE || source.getState().equals(offBlock.getValue())) {
            event.getNeighbors().entrySet().stream().map(
                    (Function<Entry<Direction, BlockState>, Location>) entry -> source.getLocation().get().getRelative(entry.getKey())).
                    collect(Collectors.toList()).stream().forEach(block -> {

                if (block.getBlock().get(Keys.POWER).isPresent()) {
                    updateState(source.getLocation().get(), block.getBlock().get(Keys.POWER).get() > 0);
                }
            });
        }
    }

    public void updateState(Location<?> location, boolean powered) {
        location.setBlock(powered ? BlockTypes.GLOWSTONE.getDefaultState() : offBlock.getValue());
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.GLOWSTONE;
    }
}