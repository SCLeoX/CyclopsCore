package org.cyclops.cyclopscore.modcompat.capabilities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.init.ModBase;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Registry for capabilities created by this mod.
 * @author rubensworks
 */
public class CapabilityConstructorRegistry {
    private final Multimap<Class<? extends TileEntity>, ICapabilityConstructor<?, ? extends TileEntity, ? extends TileEntity>>
            capabilityConstructorsTile = HashMultimap.create();
    private final Multimap<Class<? extends Entity>, ICapabilityConstructor<?, ? extends Entity, ? extends Entity>>
            capabilityConstructorsEntity = HashMultimap.create();
    private final Multimap<Class<? extends Item>, ICapabilityConstructor<?, ? extends Item, ? extends ItemStack>>
            capabilityConstructorsItem = HashMultimap.create();
    private final Set<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>>
            capabilityConstructorsTileSuper = Sets.newHashSet();
    private final Set<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>>
            capabilityConstructorsEntitySuper = Sets.newHashSet();
    private final Set<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>>
            capabilityConstructorsItemSuper = Sets.newHashSet();

    protected final ModBase mod;

    public CapabilityConstructorRegistry(ModBase mod) {
        this.mod = mod;
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected ModBase getMod() {
        return mod;
    }

    /**
     * Register a tile capability constructor.
     * @param clazz The tile class.
     * @param constructor The capability constructor.
     * @param <T> The tile type.
     */
    public <T extends TileEntity> void registerTile(Class<T> clazz, ICapabilityConstructor<?, T, T> constructor) {
        capabilityConstructorsTile.put(clazz, constructor);
    }

    /**
     * Register an entity capability constructor.
     * @param clazz The entity class.
     * @param constructor The capability constructor.
     * @param <T> The entity type.
     */
    public <T extends Entity> void registerEntity(Class<T> clazz, ICapabilityConstructor<?, T, T> constructor) {
        capabilityConstructorsEntity.put(clazz, constructor);
    }

    /**
     * Register an item capability constructor.
     * @param clazz The item class.
     * @param constructor The capability constructor.
     * @param <T> The item type.
     */
    public <T extends Item> void registerItem(Class<T> clazz, ICapabilityConstructor<?, T, ItemStack> constructor) {
        capabilityConstructorsItem.put(clazz, constructor);
    }

    /**
     * Register a tile capability constructor with subtype checking.
     * Only call this when absolutely required, this will is less efficient than its non-inheritable counterpart.
     * @param clazz The tile class, all subtypes will be checked.
     * @param constructor The capability constructor.
     * @param <K> The capability type.
     * @param <V> The tile type.
     */
    public <K, V> void registerInheritableTile(Class<K> clazz, ICapabilityConstructor<?, V, V> constructor) {
        capabilityConstructorsTileSuper.add(
                Pair.<Class<?>, ICapabilityConstructor<?, ?, ?>>of(clazz, constructor));
    }

    /**
     * Register an entity capability constructor with subtype checking.
     * Only call this when absolutely required, this will is less efficient than its non-inheritable counterpart.
     * @param clazz The tile class, all subtypes will be checked.
     * @param constructor The capability constructor.
     * @param <K> The capability type.
     * @param <V> The entity type.
     */
    public <K, V> void registerInheritableEntity(Class<K> clazz, ICapabilityConstructor<?, V, V> constructor) {
        capabilityConstructorsEntitySuper.add(
                Pair.<Class<?>, ICapabilityConstructor<?, ?, ?>>of(clazz, constructor));
    }

    /**
     * Register an item capability constructor with subtype checking.
     * Only call this when absolutely required, this will is less efficient than its non-inheritable counterpart.
     * @param clazz The tile class, all subtypes will be checked.
     * @param constructor The capability constructor.
     * @param <T> The tile type.
     */
    public <T> void registerInheritableItem(Class<T> clazz, ICapabilityConstructor<?, ?, ? extends ItemStack> constructor) {
        capabilityConstructorsItemSuper.add(
                Pair.<Class<?>, ICapabilityConstructor<?, ?, ?>>of(clazz, constructor));
    }

    @SuppressWarnings("unchecked")
    protected <K, KE, H, HE> ICapabilityProvider createProvider(KE hostType, HE host, ICapabilityConstructor<?, K, H> capabilityConstructor) {
        return capabilityConstructor.createProvider((K) hostType, (H) host);
    }

    protected <T> void onLoad(Multimap<Class<? extends T>, ICapabilityConstructor<?, ? extends T, ? extends T>> allConstructors,
                              Set<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>> allInheritableConstructors,
                              T object, AttachCapabilitiesEvent event) {
        onLoad(allConstructors, allInheritableConstructors, object, object, event);
    }

    protected <K, V> void onLoad(Multimap<Class<? extends K>, ICapabilityConstructor<?, ? extends K, ? extends V>> allConstructors,
                                 Set<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>> allInheritableConstructors,
                                 K keyObject, V valueObject, AttachCapabilitiesEvent event) {
        boolean canRemove = Helpers.isMinecraftInitialized();

        Collection<ICapabilityConstructor<?, ? extends K, ? extends V>> constructors = allConstructors.get((Class<? extends K>) keyObject.getClass());
        List<ICapabilityConstructor<?, ? extends K, ? extends V>> toRemoveList = Lists.newArrayList();
        for(ICapabilityConstructor<?, ? extends K, ? extends V> constructor : constructors) {
            if (constructor.getCapability() == null) {
                if (canRemove) {
                    toRemoveList.add(constructor);
                }
            } else {
                addLoadedCapabilityProvider(event, keyObject, valueObject, constructor);
            }
        }
        for (ICapabilityConstructor<?, ? extends K, ? extends V> toRemove : toRemoveList) {
            constructors.remove(toRemove);
        }


        List<Pair<Class<?>, ICapabilityConstructor<?, ?, ?>>> toRemoveInheritableList = Lists.newArrayList();
        for (Pair<Class<?>, ICapabilityConstructor<?, ?, ?>> constructorEntry : allInheritableConstructors) {
            if (constructorEntry.getRight().getCapability() == null) {
                if (canRemove) {
                    toRemoveInheritableList.add(constructorEntry);
                }
            } else {
                if (constructorEntry.getLeft().isInstance(keyObject)) {
                    ICapabilityConstructor<?, ?, ?> constructor = constructorEntry.getRight();
                    addLoadedCapabilityProvider(event, keyObject, valueObject, constructor);
                }
            }
        }
        for (Pair<Class<?>, ICapabilityConstructor<?, ?, ?>> toRemove : toRemoveInheritableList) {
            allInheritableConstructors.remove(toRemove);
        }
    }

    protected <K, V> void addLoadedCapabilityProvider(AttachCapabilitiesEvent event, K keyObject, V valueObject, ICapabilityConstructor<?, ?, ?> constructor) {
        ICapabilityProvider provider = createProvider(keyObject, valueObject, constructor);
        if (provider != null) {
            ResourceLocation id = new ResourceLocation(getMod().getModId(), constructor.getCapability().getName());
            event.addCapability(id, provider);
        }
    }

    @SubscribeEvent
    public void onTileLoad(AttachCapabilitiesEvent.TileEntity event) {
        onLoad(capabilityConstructorsTile, capabilityConstructorsTileSuper, event.getTileEntity(), event);
    }

    @SubscribeEvent
    public void onEntityLoad(AttachCapabilitiesEvent.Entity event) {
        onLoad(capabilityConstructorsEntity, capabilityConstructorsEntitySuper, event.getEntity(), event);
    }

    @SubscribeEvent
    public void onItemStackLoad(AttachCapabilitiesEvent.Item event) {
        onLoad(capabilityConstructorsItem, capabilityConstructorsItemSuper, event.getItem(), event.getItemStack(), event);
    }
}