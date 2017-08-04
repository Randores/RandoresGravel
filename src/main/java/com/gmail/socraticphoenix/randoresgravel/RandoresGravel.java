/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 socraticphoenix@gmail.com
 * Copyright (c) 2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.randoresgravel;

import com.gmail.socraticphoenix.randores.Randores;
import com.gmail.socraticphoenix.randores.RandoresItemRegistry;
import com.gmail.socraticphoenix.randores.block.RandoresOre;
import com.gmail.socraticphoenix.randores.component.MaterialDefinition;
import com.gmail.socraticphoenix.randores.component.OreComponent;
import com.gmail.socraticphoenix.randores.component.enumerable.MaterialType;
import com.gmail.socraticphoenix.randores.component.enumerable.OreType;
import com.gmail.socraticphoenix.randores.component.enumerable.OreTypeGenerator;
import com.gmail.socraticphoenix.randores.component.enumerable.OreTypeRegistry;
import com.gmail.socraticphoenix.randores.component.post.MaterialDefinitionEditor;
import com.gmail.socraticphoenix.randores.component.post.MaterialDefinitionEditorRegistry;
import com.gmail.socraticphoenix.randores.item.RandoresItemBlock;
import com.gmail.socraticphoenix.randores.lib.collect.Items;
import com.gmail.socraticphoenix.randores.plugin.AbstractRandoresPlugin;
import com.gmail.socraticphoenix.randores.plugin.RandoresAddon;
import com.gmail.socraticphoenix.randores.plugin.RandoresAddonProvider;
import com.gmail.socraticphoenix.randores.plugin.RandoresPlugin;
import com.gmail.socraticphoenix.randores.probability.RandoresProbability;
import com.gmail.socraticphoenix.randoresgravel.proxy.Proxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.DimensionType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod(modid = "randoresgravel", dependencies = "required-after:randores")
@RandoresAddon
public class RandoresGravel extends AbstractRandoresPlugin {
    public static final OreType GRAVEL_ORE = new OreType(w -> w.provider.getDimensionType() == DimensionType.NETHER || w.provider.getDimensionType() == DimensionType.OVERWORLD,
            "randoresgravel:gravel_ore",
            s -> s.getBlock() == Blocks.GRAVEL);

    @SidedProxy(modId = "randoresgravel", clientSide = "com.gmail.socraticphoenix.randoresgravel.proxy.ClientProxy", serverSide = "com.gmail.socraticphoenix.randoresgravel.proxy.ServerProxy")
    public static Proxy PROXY;
    public static RandoresGravel INSTANCE;

    public RandoresGravel() {
        RandoresGravel.INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent ev) {
        PROXY.init();
    }

    public static List<RandoresOre> ores;
    public static List<RandoresItemBlock> itemBlocks;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> ev) {
        ores = new ArrayList<>();
        for (MaterialType type : Randores.getDefaultMaterials()) {
            RandoresOre gravelOre = new RandoresOre(Material.SAND, GRAVEL_ORE, type);
            gravelOre.setUnlocalizedName(type.getOreName() + "_gravel").setRegistryName("randoresgravel", type.getOreName() + "_gravel");
            ev.getRegistry().register(gravelOre);
            RandoresItemRegistry.register(gravelOre);
            ores.add(gravelOre);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> ev) {
        itemBlocks = new ArrayList<>();
        for (RandoresOre ore : ores) {
            MaterialType type = ore.getMaterialType();
            RandoresItemBlock itemBlock = new RandoresItemBlock(ore);
            itemBlock.setUnlocalizedName(type.getOreName() + "_gravel").setRegistryName("randoresgravel", type.getOreName() + "_gravel");
            itemBlocks.add(itemBlock);
        }
    }

    @SubscribeEvent
    public void onModelLoad(ModelRegistryEvent ev) {
        for(RandoresItemBlock item : itemBlocks) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("randoresgravel:" + item.getUnlocalizedName().substring(5), "inventory"));
        }
    }

    @RandoresAddonProvider
    public static RandoresGravel instance() {
        return RandoresGravel.INSTANCE;
    }

    @Override
    public void registerOreTypes(OreTypeRegistry oreTypeRegistry) {
        oreTypeRegistry.register(GRAVEL_ORE);
        oreTypeRegistry.register(new OreTypeGenerator() {
            @Override
            public List<OreType> generate(Random random) {
                return Items.buildList(GRAVEL_ORE);
            }

            @Override
            public boolean test(Random random) {
                return RandoresProbability.percentChance(30, random);
            }

            @Override
            public RandoresPlugin parent() {
                return INSTANCE;
            }
        });
    }

    @Override
    public void registerEditors(MaterialDefinitionEditorRegistry editorRegistry) {
        editorRegistry.register(new MaterialDefinitionEditor() {
            @Override
            public void edit(MaterialDefinition materialDefinition, Random random) {
                if(materialDefinition.getOre().getOreType() == GRAVEL_ORE) {
                    OreComponent ore = materialDefinition.getOre();
                    int minOc = ore.getMinOccurrences();
                    int maxOc = ore.getMaxOccurrences();

                    ore.setMinOccurrences(minOc + 10); //Edit the occurrences so that it's common enough to spawn
                    ore.setMaxOccurrences(maxOc + 10);
                }
            }

            @Override
            public RandoresPlugin parent() {
                return INSTANCE;
            }
        });
    }

}
