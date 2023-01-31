package caittastic.LPipes;

import caittastic.LPipes.block.HupwardserBlock;
import caittastic.LPipes.blockentity.HupwardserBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static caittastic.LPipes.LPipes.MODID;

public class Registries {
    //registries
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);


    //utilities
    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties().tab(tab)));
        return toReturn;
    }

    //registering blocks
    public static final RegistryObject<Block> HUPWARDSER =
            registerBlockWithItem("hupwardser", () ->
                    new HupwardserBlock(BlockBehaviour.Properties.of(Material.STONE).noOcclusion()), CreativeModeTab.TAB_REDSTONE);

    //registering block entities
    public static final RegistryObject<BlockEntityType<HupwardserBlockEntity>> HUPWARDSER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("hupwardser_block_entity", () ->
                    BlockEntityType.Builder.of(HupwardserBlockEntity::new, HUPWARDSER.get()).build(null));

}
