package ru.geekbrains.entities.equipment;

import com.badlogic.gdx.math.Vector2;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import ru.geekbrains.entities.objects.GameObject;

/**
 * Ballistic processing unit
 */
public class BPU {

    private AimFunctionGun gf;
    private AimFunctionMissile mf;
    private UnivariateSolver nonBracketing;

   public GuideResult guideResult = new GuideResult();

    public BPU() {


        final double relativeAccuracy = 1.0e-10;
        final double absoluteAccuracy = 1.0e-8;

        gf =  new AimFunctionGun();
        mf =  new AimFunctionMissile();
        nonBracketing = new BrentSolver(relativeAccuracy, absoluteAccuracy);
    }


    protected static class AimFunctionGun implements UnivariateFunction {

        double rx, ry, vx, vy, ax, ay, VCC;

        public double value(double t) {

            double result = Math.pow(rx,2) + Math.pow(ry,2) + ((Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4))/4. +
                    Math.pow(t,3)*(ax*vx + ay*vy) + 2*t*(rx*vx + ry*vy) +
                    Math.pow(t,2)*(ax*rx + ay*ry - Math.pow(VCC,2) + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }


    protected static class AimFunctionMissile implements UnivariateFunction {

        double rx, ry, vx, vy, ax, ay, ACC;

        public double value(double t) {

            double result = 4*Math.pow(rx,2) + 4*Math.pow(ry,2) + (-Math.pow(ACC,2) + Math.pow(ax,2) + Math.pow(ay,2))*Math.pow(t,4) + 4*Math.pow(t,3)*(ax*vx + ay*vy) +
                    8*t*(rx*vx + ry*vy) + 4*Math.pow(t,2)*(ax*rx + ay*ry + Math.pow(vx,2) + Math.pow(vy,2));

            return result;
        }
    }

    public void guideGun(GameObject owner, GameObject target, float maxVel, float dt) {

        // Система наведения для  minigun  (пушки)
        // https://gamedev.stackexchange.com/questions/149327/projectile-aim-prediction-with-acceleration

        if (owner == null || owner.readyToDispose ||
            target == null || target.readyToDispose) {

            System.out.println(owner);
            System.out.println(target);

            System.out.println(owner.readyToDispose);
            System.out.println(target.readyToDispose);

            throw new RuntimeException("owner == null || target == null");
        }

        // F = m*a
        // a = f / m;
        // dv = a*t
        // a = dv/t;
        // f/m = dv/t
        // dv = f/m*t - Импульс силы, деленный на массу пули
        // dv ~ v

        // float maxVel = gun_power / projectile_mass * dt;




        // t - target
        // s - object


        //at - as -> a
        //rt - rs -> r
        //vt - vs -> v


        gf.VCC = maxVel;


        // ORIGINAL
        // r =  rt - rs
        gf.rx = target.pos.x - owner.pos.x;
        gf.ry = target.pos.y - owner.pos.y;

        //  relative target velocity to object
        gf.vx = target.vel.x - owner.vel.x;
        gf.vy = target.vel.y - owner.vel.y;

        gf.ax = target.acc.x - owner.acc.x;
        gf.ay = target.acc.y - owner.acc.y;




        guideResult.impactTime = Double.NaN;
        guideResult.target = null;
        guideResult.guideVector.setZero();
        guideResult.impactVector.setZero();


        // Гидра доминатус !!!!

        //double tbd = 0;
        // Цикл - отделить корни
        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (gf.value(0) > 0 && gf.value(dt * i*10) > 0 ||
                        gf.value(0) < 0 && gf.value(dt * i*10) < 0) {

                    continue;
                }

                double t = nonBracketing.solve(100, gf,  0, dt * i*10);


                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double vs_x = gf.rx / t + 0.5 * gf.ax * t + gf.vx;
                    double vs_y = gf.ry / t + 0.5 * gf.ay * t + gf.vy;

                    double rs_x = gf.rx + vs_x * t + 0.5 * gf.ax * t*t;
                    double rs_y = gf.ry + vs_y * t + 0.5 * gf.ay * t*t;

                    guideResult.guideVector.set((float) vs_x, (float) vs_y);
                    guideResult.impactVector.set((float) rs_x, (float) rs_y);
                    guideResult.impactTime = t;
                    guideResult.target = target;

                    break;
                }
            }catch (Exception ignore) {}

        }


    }

    public void guideMissile(GameObject owner, GameObject target, float maxAcc, float dt) {

        if (owner == null || owner.readyToDispose ||
                target == null || target.readyToDispose) {

            System.out.println(owner);
            System.out.println(target);

            throw new RuntimeException("owner == null || target == null");
        }

        mf.ACC = maxAcc;


        // t - target
        // s - object


        //at      -> a
        //rt - rs -> r
        //vt - vs -> v



        // r =  rt - rs
        mf.rx = target.pos.x - owner.pos.x;
        mf.ry = target.pos.y - owner.pos.y;

        // v =  vt - vs
        mf.vx = target.vel.x - owner.vel.x;
        mf.vy = target.vel.y - owner.vel.y;

        // EXPERIMENTAL
        // removed applied inverted object acceleration to target
        // почему так лучше работает - я хз
        mf.ax = target.acc.x /*- owner.acc.x*/;
        mf.ay = target.acc.y /*- owner.acc.y*/;

        guideResult.impactTime = Double.NaN;
        guideResult.target = null;
        guideResult.guideVector.setZero();
        guideResult.impactVector.setZero();

        for (int i = 0; i< 100; i++) {
            try {

                // Корней нет - функция не пересекает ось Ox
                if (mf.value(0) > 0 && mf.value(dt * i*10) > 0 ||
                        mf.value(0) < 0 && mf.value(dt * i*10) < 0) {

                    continue;
                }

                // Для ракет сделаем погрешность в вычислениях побольше
                double t = nonBracketing.solve(100, mf,  0, dt * i*10);

                if (!Double.isNaN(t) && !Double.isInfinite(t) && t > 0) {

                    double as_x = 2 * mf.rx / t + mf.ax * t + 2 * mf.vx;
                    double as_y = 2 * mf.ry / t + mf.ay * t + 2 * mf.vy;

                    double vs_x = mf.vx + as_x * t;
                    double vs_y = mf.vy + as_y * t;

                    double rs_x = mf.rx + vs_x * t + 0.5 * mf.ax * t*t;
                    double rs_y = mf.ry + vs_y * t + 0.5 * mf.ay * t*t;

                    guideResult.guideVector.set((float) as_x, (float) as_y);
                    guideResult.impactVector.set((float) rs_x, (float) rs_y);
                    guideResult.impactTime = t;
                    guideResult.target = target;

                    break;
                }
            }
            catch (Exception ignore) {}

        }


    }




    public static class GuideResult {

        public double impactTime;
        /**
         * Vector needed to owner to lock target
         */
        public Vector2 guideVector = new Vector2();

        /**
         * impact vector
         */
        public Vector2 impactVector = new Vector2();

        public GameObject target = null;

        public GuideResult clone() {

            GuideResult result = new GuideResult();

            result.impactTime = impactTime;
            result.guideVector = guideVector.cpy();
            result.impactVector = impactVector.cpy();
            result.target = target;
            
            return result;
        }
    }

}
