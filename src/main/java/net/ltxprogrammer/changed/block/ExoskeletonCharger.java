package net.ltxprogrammer.changed.block;

import net.ltxprogrammer.changed.entity.robot.AbstractRobot;
import net.ltxprogrammer.changed.entity.robot.ChargerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.Random;

public class ExoskeletonCharger extends AbstractLargePanel implements IRobotCharger {
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public ExoskeletonCharger() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(6.5F, 9.0F)
                .randomTicks());
        this.registerDefaultState(this.defaultBlockState()
                .setValue(OCCUPIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OCCUPIED);
    }

    @Override
    public ChargerType getChargerType() {
        return ChargerType.EXOSKELETON;
    }

    @Override
    public void acceptRobot(BlockState state, Level level, BlockPos pos, AbstractRobot robot) {

    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        super.randomTick(state, level, pos, random);
        if (state.getValue(SECTION) == NineSection.CENTER)
            broadcastPosition(level, pos, !state.getValue(OCCUPIED));
    }
}
