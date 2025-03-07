package arathain.mason.entity;

import arathain.mason.MasonDecorClient;
import arathain.mason.entity.goal.RavenDeliverBundleGoal;
import arathain.mason.entity.goal.RavenFollowOwnerGoal;
import arathain.mason.init.MasonObjects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class RavenEntity extends TameableEntity implements GeoAnimatable {
    private static final TrackedData<Optional<UUID>> RECEIVER_UUID = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    public static final TrackedData<String> TYPE = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.STRING);
    private final AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> GOING_TO_RECEIVER = DataTracker.registerData(RavenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public RavenEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        moveControl = new FlightMoveControl(this, 90, false);

        if (!world.isClient) {
            this.setRavenType(Type.getRandomType(this.random));
        }
    }

    public float maxWingDeviation;
    public float prevMaxWingDeviation;
    private float whuhhuh = 1.0f;
    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(3, new RavenDeliverBundleGoal<>(this, 1, 6, 128, false));
        goalSelector.add(2, new SitGoal(this));
        goalSelector.add(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.add(4, new RavenFollowOwnerGoal(this, 1, 10, 2, false));
        goalSelector.add(5, new AnimalMateGoal(this, 1));
        goalSelector.add(6, new WanderAroundFarGoal(this, 1));
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8));
        goalSelector.add(7, new LookAroundGoal(this));
        targetSelector.add(0, new TrackOwnerAttackerGoal(this));
        targetSelector.add(1, new AttackWithOwnerGoal(this));
        targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
    }
    public static DefaultAttributeContainer.Builder createRavenAttributes() {
        return MobEntity.createAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7);
    }

    @Override
    protected void initEquipment(RandomGenerator randomGenerator, LocalDifficulty difficulty) {
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 1);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("sitting", isSitting());
        nbt.putBoolean("goin", dataTracker.get(GOING_TO_RECEIVER));
        if (this.getReceiverUuid() != null) {
            nbt.putUuid("Receiver", this.getReceiverUuid());
        }
        nbt.putString("Type", this.getRavenType().toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setSitting(nbt.getBoolean("sitting"));
        dataTracker.set(GOING_TO_RECEIVER, nbt.getBoolean("goin"));

        if (nbt.contains("Type")) {
            try {
                this.setRavenType(Type.valueOf(nbt.getString("Type")));
            } catch (IllegalArgumentException e) {
                this.setRavenType(Type.DARK);
            }
        }

        if (nbt.containsUuid("Receiver")) {
            setReceiverUuid(nbt.getUuid("Receiver"));
        } else {
            String string = nbt.getString("Receiver");
            setReceiverUuid(ServerConfigHandler.getPlayerUuidByName(this.getServer(), string));
        }
    }

    public Type getRavenType() {
        return Type.valueOf(this.dataTracker.get(TYPE));
    }

    public void setRavenType(Type type) {
        this.dataTracker.set(TYPE, type.toString());
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(RECEIVER_UUID, Optional.empty());
        this.dataTracker.startTracking(GOING_TO_RECEIVER, false);
        this.dataTracker.startTracking(TYPE, Type.DARK.toString());
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return ((stack.getItem().getFoodComponent() != null && stack.getItem().getFoodComponent().isMeat()));
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        ItemStack stack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if(!stack.isEmpty() && stack.hasCustomName()) {
            PlayerEntity entity = getServer().getPlayerManager().getPlayer(stack.getName().getString());
            if(entity != null && entity.getUuid() != null && !stack.getName().toString().contains("Mouthpiece")) {
                this.setReceiverUuid(entity.getUuid());
            } else {
                this.setReceiverUuid(null);
                this.dataTracker.set(GOING_TO_RECEIVER, false);
            }
        } else {
            this.setReceiverUuid(null);
            this.dataTracker.set(GOING_TO_RECEIVER, false);
        }
        if (this.hasCustomName()) {
            String name = this.getCustomName().getString();
            if (name.equalsIgnoreCase("three_eyed") || name.equalsIgnoreCase("three_eyed_raven") || name.equalsIgnoreCase("three eyed") || name.equalsIgnoreCase("three eyed raven") || name.equalsIgnoreCase("three-eyed raven")) {
                this.setRavenType(Type.THREE_EYED);
            }
        }
    }


    public void spawnFeatherParticles(int count) {
        if(getWorld().isClient) {
            float height = this.getHeight();
            if (height * 100 < 100) height = 1.0F;
            else height = height + 0.5F;
            for (int i = 0; i <= count; i++) {
                double randomHeight = (double) this.random.nextInt((int) height * 10) / 10;
                getWorld().addParticle(
                        switch(this.getRavenType()) {
                            case DARK, THREE_EYED -> MasonDecorClient.RAVEN_FEATHER;
                            case ALBINO -> MasonDecorClient.RAVEN_FEATHER_ALBINO;
                            case SEA_GREEN -> MasonDecorClient.RAVEN_FEATHER_GREEN;
                        },
                        this.getX(), this.getY() + 0.2D + randomHeight, this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if(stack.getItem().equals(Items.BUNDLE) && stack.hasCustomName()) {
            if (!this.getWorld().isClient) {
                this.equipStack(EquipmentSlot.MAINHAND, stack.copy());
                player.setStackInHand(hand, ItemStack.EMPTY);
                ItemStack stack2 = this.getEquippedStack(EquipmentSlot.MAINHAND);
                if (!stack2.isEmpty() && stack2.hasCustomName()) {
                    PlayerEntity entity = getServer().getPlayerManager().getPlayer(stack2.getName().getString());
                    if (entity != null && entity.getUuid() != null && !stack2.getName().toString().contains("Mouthpiece")) {
                        this.setReceiverUuid(entity.getUuid());
                    } else {
                        this.setReceiverUuid(null);
                        this.dataTracker.set(GOING_TO_RECEIVER, false);
                    }
                } else {
                    this.setReceiverUuid(null);
                    this.dataTracker.set(GOING_TO_RECEIVER, false);
                }
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        if(stack.isEmpty() && this.getStackInHand(Hand.MAIN_HAND).getItem().equals(Items.BUNDLE) && !player.isSneaking()) {
            if (!this.getWorld().isClient) {
                player.setStackInHand(hand, this.getStackInHand(Hand.MAIN_HAND));
                this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        if (this.isOnGround() && this.isTamed() && this.isOwner(player) && stack.isEmpty()) {
            if (!this.getWorld().isClient) {
                this.setSitting(!this.isSitting());
            }

            return ActionResult.success(this.getWorld().isClient);
        }
        if (!isTamed()) {
            if (isBreedingItem(stack)) {
                if (!getWorld().isClient()) {
                    eat(player, hand, stack);
                    if (random.nextInt(4) == 0) {
                        setOwner(player);
                        setSitting(true);
                        setTarget(null);
                        navigation.stop();
                        getWorld().sendEntityStatus(this, (byte) 7);
                    } else {
                        getWorld().sendEntityStatus(this, (byte) 6);
                    }
                }
                return ActionResult.success(getWorld().isClient());
            }
        } else if (isBreedingItem(stack)) {
            if (getHealth() < getMaxHealth()) {
                if (!getWorld().isClient()) {
                    eat(player, hand, stack);
                    heal(4);
                }
                return ActionResult.success(getWorld().isClient());
            }
        }
        return super.interactMob(player, hand);
    }

    public UUID getReceiverUuid() {
        return (UUID) ((Optional) this.dataTracker.get(RECEIVER_UUID)).orElse(null);
    }

    public void setReceiverUuid(@Nullable UUID uuid) {
        this.dataTracker.set(RECEIVER_UUID, Optional.ofNullable(uuid));
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15f, 1);
    }
    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void addFlapEffects() {
        if(!isSitting())
            playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15f, 1);
        this.whuhhuh = this.flyDistance + this.maxWingDeviation / 2.0f;
    }
    @Override
    public void tickMovement() {
        super.tickMovement();
        this.flapWings();
    }
    private void flapWings() {
        this.prevMaxWingDeviation = this.maxWingDeviation;
        this.maxWingDeviation += (float)(this.isOnGround() || this.hasVehicle() ? -1 : 4) * 0.3f;
        this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0f, 1.0f);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    protected boolean hasWings() {
        return this.flyDistance > this.whuhhuh;
    }


    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        RavenEntity child = MasonObjects.RAVEN.create(world);
        if (child != null) {
            child.initialize(world, world.getLocalDifficulty(getBlockPos()), SpawnReason.BREEDING, null, null);
            UUID owner = getOwnerUuid();
            if (owner != null) {
                child.setOwnerUuid(owner);
                child.setTamed(true);
            }
            if (entity instanceof RavenEntity && random.nextFloat() < 0.95f) {
                child.dataTracker.set(TYPE, random.nextBoolean() ? dataTracker.get(TYPE) : entity.getDataTracker().get(TYPE));
            }
        }
        return child;
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        RawAnimation animationBuilder = RawAnimation.begin();
        if (!this.isOnGround()) {
            animationBuilder.thenLoop(Math.abs(this.getVelocity().y) > 0.1f ? "fastFly" : "fly");
            event.getController().setAnimation(animationBuilder);
        } else if (!(Boolean)this.dataTracker.get(SITTING) && !this.hasVehicle()) {
            animationBuilder.thenLoop("idle");
            event.getController().setAnimation(animationBuilder);
        } else {
            animationBuilder.thenLoop("sitIdle");
            event.getController().setAnimation(animationBuilder);
        }
        
        return PlayState.CONTINUE;
    }


    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof TameableEntity && ((TameableEntity) target).isTamed()) {
            return false;
        }
        if (target instanceof HorseBaseEntity && ((HorseBaseEntity) target).isTame()) {
            return false;
        }
        if (target instanceof PlayerEntity player && owner instanceof PlayerEntity playerOwner && !playerOwner.shouldDamagePlayer(player)) {
            return false;
        }
        return !(target instanceof CreeperEntity) && !(target instanceof GhastEntity);
    }

    @Override
    public boolean isSitting() {
        return this.dataTracker.get(SITTING);
    }

    @Override
    public void setSitting(boolean sitting) {
        this.dataTracker.set(SITTING, sitting);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        } else {
            spawnFeatherParticles(3);
            Entity entity = source.getAttacker();
            setSitting(false);
            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof PersistentProjectileEntity)) {
                amount = (amount + 1) / 2f;
            }
            return super.damage(source, amount);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return MasonObjects.ENTITY_RAVEN_CAW;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MasonObjects.ENTITY_RAVEN_CAW;
    }

    @Override
       public EntityView getEntityView() {
        return this.getWorld();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.factory;
    }

    @Override
    public double getTick(Object object) {
        return this.age;
    }

    public enum Type {
        DARK(55),      // 55% chance
        ALBINO(10),    // 10% chance
        SEA_GREEN(35), // 35% chance
        THREE_EYED(0); // 0% chance cuz nametag variant

        private final int weight;
        private static final Random RANDOM = new Random();

        Type(int weight) {
            this.weight = weight;
        }

        public static Type getRandomType(RandomGenerator random) {
            int totalWeight = 0;
            for (Type type : Type.values()) {
                totalWeight += type.weight;
            }

            int randomValue = RANDOM.nextInt(totalWeight);
            int currentWeight = 0;

            for (Type type : Type.values()) {
                currentWeight += type.weight;
                if (randomValue < currentWeight) {
                    return type;
                }
            }

            return DARK; // Fallback if shii dont work or sumfing
        }
    }
}
