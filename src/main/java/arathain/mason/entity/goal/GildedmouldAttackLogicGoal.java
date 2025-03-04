package arathain.mason.entity.goal;

import arathain.mason.entity.GildedmouldEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class GildedmouldAttackLogicGoal extends Goal {
    private final GildedmouldEntity mould;
    private int scrunkly;
    private double targetX;
    private double targetY;
    private double targetZ;

    public GildedmouldAttackLogicGoal(GildedmouldEntity entity) {
        this.mould = entity;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    public boolean canStart() {
        LivingEntity target = this.mould.getTarget();
        return target != null && target.isAlive() && !this.mould.isDormant();
    }

    public void start() {
        this.scrunkly = 0;
    }

    public void stop() {
        this.mould.getNavigation().stop();
    }

    public void tick() {
        LivingEntity target = this.mould.getTarget();
              if (target != null) {
            double distance = this.mould.squaredDistanceTo(this.targetX, this.targetY, this.targetZ);
            if (--this.scrunkly <= 0 && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || target.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0) || this.mould.getNavigation().isIdle()) {
                this.targetX = target.getX();
                this.targetY = target.getY();
                this.targetZ = target.getZ();
                this.scrunkly = 4 + this.mould.getRandom().nextInt(6);
                if (distance > 1024.0) {
                    this.scrunkly += 10;
                } else if (distance > 256.0) {
                    this.scrunkly += 5;
                }

                if (!this.mould.getNavigation().startMovingTo(target, 0.5)) {
                    this.scrunkly += 15;
                }
            }
                  
                   distance = this.mould.squaredDistanceTo(this.targetX, this.targetY, this.targetZ);
            if (target.getY() - this.mould.getY() >= -1.0 && target.getY() - this.mould.getY() <= 3.0 && Math.abs(MathHelper.wrapDegrees(this.mould.getAngleBetweenEntities(target, this.mould) - (double)this.mould.getYaw())) < 35.0) {
                this.mould.setAttackState(2);
                this.mould.dashSlashTicks = 0;
            }
                  
        }
    }
}
