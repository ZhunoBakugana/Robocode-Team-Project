package pillars;

import java.awt.Color;
import java.util.Random;

import robocode.*;
import robocode.util.Utils;

public class Amaterasu extends Robot {

    static Random random = new Random();

    // Energy threshold constants written by Teammate 1
    private static final double HIGH_ENERGY     = 70.0;
    private static final double MEDIUM_ENERGY   = 40.0;
    private static final double LOW_ENERGY      = 20.0;
    private static final double CRITICAL_ENERGY = 10.0;
    
    // Dodge cooldown state — Written by Teammate 1
    private long lastDodgeTime = 0;
    private static final long DODGE_COOLDOWN = 10;
    
    // Movement constants by Teammate 2
    private static final double MAX_VELOCITY    = 8.0;
    private static final double WALL_MARGIN     = 60.0;
    private static final double PREFERRED_ORBIT = 200.0;

    // Movement state by Teammate 2
    private int    orbitDirection    = 1;
    private int    strafeTimer       = 0;
    private int    strafePeriod      = 30;
    private double lastEnemyBearing  = 0;
    private double lastEnemyDistance = 300;

	public void run() {
		
		// robot colours - Teammate 3
		setBodyColor(new Color(255, 223, 76));
		setGunColor(new Color(0, 0, 0));
		setRadarColor(new Color(194, 98, 92));
		setBulletColor(new Color(182, 139, 129));
		setScanColor(new Color(199, 190, 160));
		
		// Robot main loop
		while(true) {
			if (hasEnemyData()) {
			    orbitEnemy(lastEnemyBearing, lastEnemyDistance);
            }else{
                goToCentre();
            }
			
		    strafeUpdate();
            turnRadarRight(360);
                
		}
	}
	/**
	 * behaviorByEnergy: Moves the robot based on current energy level.
	 * high energy = short aggressive moves, low energy = long passive moves
	 * written by Teammate 1
	 */
    private void behaviorByEnergy() {
    double energy = getEnergy();
    if (energy > LOW_ENERGY) {
        ahead(200);
       turnRight(20);
    } else if (energy > CRITICAL_ENERGY) {
        ahead(300);
       turnRight(30);
    } else {
        ahead(500);
       turnRight(45);
    }
    }

	public void onScannedRobot(ScannedRobotEvent e) { //What to do when an enemy bot is scanned
		// gets enemies angle
	    double enemyBearing = e.getBearing();
	    
	    // Gets enemies distance from your tank
	    double target_distance = e.getDistance();

	    //Store enemy data for movement module by Teammate 2
        lastEnemyBearing  = getHeading() + enemyBearing;
        lastEnemyDistance = target_distance;
	    
	    // gets enemies x and y position using Math
	    double x = getX() + target_distance * Math.sin(Math.toRadians(getHeading() + enemyBearing));
	    double y = getY() + target_distance * Math.cos(Math.toRadians(getHeading() + enemyBearing));
	    
	    stop(); //stops the tank from moving.
	    
		//Dynamic turning depending on if the enemy is close of far. this was done to ensure that the enemy is not lost as often.
		if(target_distance <= 200){
			turnRadarLeft(enemyBearing + 40);
		} else if (target_distance <= 400) {
			turnRadarLeft(enemyBearing + 20);
		} else if(target_distance <= 600) {
			turnRadarLeft(enemyBearing + 10); 
		} else {
			turnRadarLeft(enemyBearing);
		} 
		// fire power depends on if distance is closer = more damage
		// written by Teammate 1
		double firePower = 0.0;
		if (target_distance < 200) {
		   firePower = 3.0;
		} else if (target_distance < 400) {
		   firePower = 2.0;
		} else if (target_distance < 600) {
		   firePower = 1.0;
		} else {
		   firePower = 0.5;
		}
	    predictiveFiring(e.getHeading(), e.getVelocity(), target_distance,firePower, x, y );
	    
	    resume();// resumes tank movement

        //since we're not using behvaiourByEnergy() anymore I don't think stop() and resume() are needed, but I'll keep them here
	    
	}

	/**
	 * onHitByBullet: Dodges follow up bullets by moving perpendicular to the hit.
	 * retreats further if energy is critically low.
	 * written by Teammate 1
	 */
	public void onHitByBullet(HitByBulletEvent e) {
	    long now = getTime();
	    if (now - lastDodgeTime < DODGE_COOLDOWN) return;
	    lastDodgeTime = now;
	    double bearing = e.getBearing();
	    orbitDirection *= -1;   // flip orbit so follow-up predicted shots miss
	    if (getEnergy() > LOW_ENERGY) {
	       turnRight(Utils.normalRelativeAngleDegrees(bearing + 90));
	        ahead(100);
	    } else {
	       turnRight(Utils.normalRelativeAngleDegrees(bearing + 135));
	        back(200);
	    }
	}
	/**
	 * onBulletHit: Fires a follow up shot when we confirm a hit on an enemy
	 * bullet power scales down as our own energy drops.
	 * written by Teammate 1
	 */
      	public void onBulletHit(BulletHitEvent e) {
	       double myEnergy = getEnergy();
	    double firePower;
        if (myEnergy > MEDIUM_ENERGY) {
	        firePower = 3.0;
	    }   else if (myEnergy > LOW_ENERGY) {
	        firePower = 2.0;
	    }   else {
	        firePower = 1.0;
	    }
            if (myEnergy > firePower + 1.0) {
	        fire(firePower);
	    }
	}
	/**
	 * onHitRobot rams enemy if we have high energy retreats if low.
	 * written by Teammate 1
	 */
	    public void onHitRobot(HitRobotEvent e) {
	    double myEnergy = getEnergy();
	    double bearing  = e.getBearing();
       if (myEnergy > MEDIUM_ENERGY) {
	      if (e.isMyFault()) {
	        fire(3.0);
	        }
	                  ahead(40);
	    } else {
	        turnRight(Utils.normalRelativeAngleDegrees(bearing + 180));
	        back(100);
	        fire(0.5);
	    }
	}
	/**
	 * onHitWall: backs away from wall and turns based on wall bearing
	 * prevents the robot getting stuck in corners
	 * written by Teammate 1
	 */
	public void onHitWall(HitWallEvent e) {
        setColors(randomColor(),randomColor(),randomColor());

	    double bearing = e.getBearing();
	    orbitDirection *= -1;   // prevent driving straight back into the same wall
	    back(50);
	    turnRight(Utils.normalRelativeAngleDegrees(-bearing));
	}
    /**
     * orbitEnemy: moves the robot perpendicular to the enemy,
     * circling them at PREFERRED_ORBIT distance (200px).
     *
     * How it works:
     * - Takes the bearing to the enemy and adds 90 degrees to get
     *   the perpendicular angle (sideways relative to enemy)
     * - Uses Utils.normalRelativeAngleDegrees to get the shortest
     *   turn needed — converts 0-360 angles into -180 to +180
     *   so the robot always turns the shorter way around
     * - Calculates speed based on how far we are from ideal distance:
     *   too far = move faster toward enemy, too close = slow down or back off
     * - clamp() enforces Robocode physics limit of 8px/tick max velocity
     *
     * Physics note: turn rate = 10 - 0.75 x velocity deg/turn,
     * so moving slower allows tighter turns.
     *
     * Written by Teammate 2.
     */
    private void orbitEnemy(double enemyBearing, double target_distance) {
        double perpendicularAngle = enemyBearing + (90.0 * orbitDirection);
        // Blend wall avoidance correction into orbital angle
        double wallCorrection = getWallAvoidanceCorrection();
        double turnNeeded = Utils.normalRelativeAngleDegrees(
            perpendicularAngle - getHeading()
        ) + wallCorrection;
  
        if (turnNeeded > 0) {
            turnRight(turnNeeded);
        } else {
            turnLeft(-turnNeeded);
        }
        double orbitRadius = getOrbitRadius();
        double distanceFactor = target_distance - orbitRadius;
        double energySpeed = getSpeedByEnergy();
        double speed = clamp(
            distanceFactor * 0.05 + 3.0, -energySpeed, energySpeed
        );
        if (speed * orbitDirection > 0) {
            ahead(Math.abs(speed) * 10);
        } else {
            back(Math.abs(speed) * 10);
        }
    }

    /**
     * strafeUpdate: randomly reverses orbit direction every 10-50 ticks.
     * Randomised interval makes movement unpredictable to the enemy.
     * Written by Teammate 2.
     */
    private void strafeUpdate() {
        strafeTimer++;
        if (strafeTimer >= strafePeriod) {
            orbitDirection *= -1;
            strafeTimer     = 0;
         // Strafe faster when low on energy — harder to predict when vulnerable
            int minPeriod = (getEnergy() < 25) ? 5 : 10;
            strafePeriod = minPeriod + (int)(Math.random() * 30);
        }
    }
    /**
     * getWallAvoidanceCorrection: calculates the turn angle
     * correction needed to avoid walls.
     * Returns a correction value instead of issuing its own turn,
     * so it blends smoothly with orbital movement.
     * Written by Teammate 2.
     */
    private double getWallAvoidanceCorrection() {
        double x        = getX();
        double y        = getY();
        double bfWidth  = getBattleFieldWidth();
        double bfHeight = getBattleFieldHeight();
        double fx = 0, fy = 0;

        if (x < WALL_MARGIN)             fx += wallForce(x);
        if (bfWidth  - x < WALL_MARGIN)  fx -= wallForce(bfWidth - x);
        if (y < WALL_MARGIN)             fy += wallForce(y);
        if (bfHeight - y < WALL_MARGIN)  fy -= wallForce(bfHeight - y);

        if (Math.abs(fx) > 0.1 || Math.abs(fy) > 0.1) {
            double escapeAngle = Math.toDegrees(Math.atan2(fx, fy));
            double correction = Utils.normalRelativeAngleDegrees(
                    escapeAngle - getHeading()
                ) * 0.4;
                // Cap correction at 45 degrees to prevent overcorrection in corners
                return clamp(correction, -45, 45);
        }
        return 0;
    }
   /**
    * wallForce: inverse-square repulsion from a single wall.
    * Closer = exponentially stronger push.
    * Written by Teammate 2.
    */
   private double wallForce(double distance) {
       if (distance < 1) distance = 1;
       return WALL_MARGIN / (distance * distance);
   }
   /**
    * goToCentre: moves robot toward the battlefield centre
    * when no enemy has been scanned yet.
    * Prevents robot orbiting a fixed point at battle start.
    * Written by Teammate 2.
    */
   private void goToCentre() {
       double centreX = getBattleFieldWidth()  / 2;
       double centreY = getBattleFieldHeight() / 2;
       double angle   = Math.toDegrees(
           Math.atan2(centreX - getX(), centreY - getY())
       );
       double turn = Utils.normalRelativeAngleDegrees(
           angle - getHeading()
       );
       if (turn > 0) {
           turnRight(turn);
       } else {
           turnLeft(-turn);
       }
       ahead(50);
   }
   /**
    * hasEnemyData: returns true once we have received at
    * least one scan from onScannedRobot.
    * Used to switch between centre patrol and orbital movement.
    * Written by Teammate 2.
    */
   private boolean hasEnemyData() {
       return lastEnemyDistance != 300;
   }
   /**
    * getSpeedByEnergy: scales max movement speed based on
    * current energy level.
    * Lower energy = slower speed = tighter turns.
    * Physics: turn rate = 10 - 0.75 x velocity,
    * so slower = more agile when survival matters most.
    * Written by Teammate 2.
    */
   private double getSpeedByEnergy() {
       double energy = getEnergy();
       if (energy > 50) {
           return MAX_VELOCITY;         // full speed when healthy
       } else if (energy > 25) {
           return MAX_VELOCITY * 0.7;   // 70% speed at medium energy
       } else {
           return MAX_VELOCITY * 0.4;   // 40% speed when low — tightest turns
       }
   }
   /**
    * getOrbitRadius: returns preferred orbit distance based on energy.
    * High energy = wider orbit for safety.
    * Low energy = tighter orbit to stay close and land more hits.
    * Physics: tighter orbit means more frequent firing opportunities.
    * Written by Teammate 2.
    */
   private double getOrbitRadius() {
       double energy = getEnergy();
       if (energy > 50) {
           return 250.0; // wide orbit when healthy — harder to hit
       } else if (energy > 25) {
           return 200.0; // standard orbit at medium energy
       } else {
           return 150.0; // tight orbit when low — maximise damage output
       }
   }
   /**
    * clamp: restricts value to min/max range.
    * Enforces Robocode physics speed limit of 8px/tick.
    * Written by Teammate 2.
    */
   private double clamp(double value, double min, double max) {
       return Math.max(min, Math.min(max, value));
   }
	
		public void predictiveFiring(double target_heading, double target_velocity, double target_distance,double firePower, double x, double y){ //turns gun based on this method's output
		double bulletSpeed = 20 - 3 * firePower;
		//we'll use these variables to keep track of the enemies position, as the below while loop progresses
		double predictedX = x;
		double predictedY = y;
		double deltaTime = 0;//will keep track of ticks

		while((++deltaTime) * bulletSpeed < target_distance){//the loop keeps going until our bullet reaches the enemy
			//estimates and updates us on the enemies's new position

			predictedX += Math.sin(Math.toRadians(target_heading)) * target_velocity;
			predictedY += Math.cos(Math.toRadians(target_heading)) * target_velocity;
			
		  
		   predictedX = Math.max(0, Math.min(getBattleFieldWidth(),  predictedX));
		   predictedY = Math.max(0, Math.min(getBattleFieldHeight(), predictedY));
		}
	

		//gives us the predicted direction of the scanned tank in radians, and then converts it to degrees
		double gunAbsHeading = Math.toDegrees(Math.atan2(predictedX - getX(), predictedY - getY()));

	    // gets the angle to turn using the util(if the util is removed the bot might do 360s instead of turning the shorter way).
	    // also turns the angles from 0-360 into -180-+180
		turnGunRight(Utils.normalRelativeAngleDegrees( gunAbsHeading - getGunHeading()));
		fire(firePower);
	}

    public static Color randomColor(){//generates a random color
        return new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256));
    }
}
