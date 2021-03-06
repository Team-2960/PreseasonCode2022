package frc.robot.SubSystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
//MOTORS
import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
//UTIL
import frc.robot.Util.Swerve;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogGyro;

//MATHU
import java.math.*;
import java.util.ArrayList;


public class Drive extends SubsystemBase {
  //TODO ADD TRAJECTORY https://docs.wpilib.org/en/stable/docs/software/examples-tutorials/trajectory-tutorial/index.html
    private static Drive drive;
    public static double frontLeftSwerveSpeed;
    public static double frontLeftSwerveAngle;
    public static double frontRightSwerveSpeed;
    public static double frontRightSwerveAngle;
    public static double backLeftSwerveSpeed;
    public static double backLeftSwerveAngle;
    public static double backRightSwerveSpeed;
    public static double backRightSwerveAngle;
    public static PIDController PIDDFL;
    public static PIDController PIDAFL;
    public static PIDController PIDDFR;
    public static PIDController PIDAFR;
    public static PIDController PIDDBL;
    public static PIDController PIDABL;
    public static PIDController PIDDBR;
    public static PIDController PIDABR;
    public static PIDController angleRatePID;
    public AHRS navX;
    public Swerve frontLeft;
    public Swerve frontRight;
    public Swerve backRight;
    public Swerve backLeft;
    public double gyroAngle;
    public AnalogGyro gyro;
    public ADXRS450_Gyro gyro2;
    public SwerveDriveOdometry m_odometry;
    public double maxTurnSpeed;
    public double angleRateVector;
    double targetAngleRate;
    public static Drive get_Instance(){
    
        if(drive == null){
          drive = new Drive();
        } 
        return drive;
      }
      public Drive(){
         navX = new AHRS(SPI.Port.kMXP);
         navX.resetDisplacement();
         gyro = new AnalogGyro(0);
         gyro2 = new ADXRS450_Gyro();

         angleRatePID = new PIDController(Constants.aRP, Constants.aRI, Constants.aRD);
         PIDDFL = new PIDController(Constants.dPFL, Constants.dIFL, Constants.dDFL);
         PIDAFL = new PIDController(Constants.aPFL, Constants.aIFL, Constants.aDFL);
         PIDDFR = new PIDController(Constants.dPFR, Constants.dIFR, Constants.dDFR);
         PIDAFR = new PIDController(Constants.aPFR, Constants.aIFR, Constants.aDFR);
         PIDDBL = new PIDController(Constants.dPBL, Constants.dIBL, Constants.dDBL);
         PIDABL = new PIDController(Constants.aPBL, Constants.aIBL, Constants.aDBL);
         PIDDBR = new PIDController(Constants.dPBR, Constants.dIBR, Constants.dDBR);
         PIDABR = new PIDController(Constants.aPBR, Constants.aIBR, Constants.aDBR);
         frontLeft = new Swerve(Constants.motorIdDriveFrontLeft, Constants.motorIdAngleFrontLeft, Constants.encoderIdFrontLeft, PIDDFL, PIDAFL, Constants.flHome);
         frontRight = new Swerve(Constants.motorIdDriveFrontRight, Constants.motorIdAngleFrontRight, Constants.encoderIdFrontRight,PIDDFR, PIDAFR, Constants.frHome);
         backRight = new Swerve(Constants.motorIdDriveBackRight, Constants.motorIdAngleBackRight, Constants.encoderIdBackRight, PIDDBR, PIDABR, Constants.brHome);
         backLeft = new Swerve(Constants.motorIdDriveBackLeft, Constants.motorIdAngleBackLeft, Constants.encoderIdBackLeft, PIDDBL, PIDABL, Constants.blHome);
         Translation2d m_frontLeftLocation = new Translation2d(0.381, 0.381);
         Translation2d m_frontRightLocation = new Translation2d(0.381, -0.381);
         Translation2d m_backLeftLocation = new Translation2d(-0.381, 0.381);
        Translation2d m_backRightLocation = new Translation2d(-0.381, -0.381);
        SwerveDriveKinematics m_kinematics = new SwerveDriveKinematics(
          m_frontLeftLocation, m_frontRightLocation, m_backLeftLocation, m_backRightLocation
        );
       m_odometry = new SwerveDriveOdometry(m_kinematics,
        new Rotation2d(gyro.getAngle()), new Pose2d(0, 0, new Rotation2d()));  
      }

      public void homeSwerve(){
        drive.setVector(180, 0, 0);
      }
      public void setSwerve(double angleVectorX, double angleVectorY, double rotationVectorX){
        double rotationVectorY = rotationVectorX;
        double A = angleVectorX - rotationVectorX;//THE PLUS AND MINUS MAY BE FLIPPED
        double B = angleVectorX + rotationVectorX;
        double C = angleVectorY - rotationVectorY;
        double D = angleVectorY + rotationVectorY;

        frontLeftSwerveSpeed = Math.sqrt(Math.pow(A,2.0) + Math.pow(C,2.0));
        frontLeftSwerveAngle = Math.atan2(A,C)*180/Math.PI;
        backLeftSwerveSpeed =  Math.sqrt(Math.pow(A,2.0) + Math.pow(D,2.0));
        backLeftSwerveAngle = Math.atan2(A,D)*180/Math.PI;
        frontRightSwerveSpeed =  Math.sqrt(Math.pow(B,2.0) + Math.pow(C,2.0));
        frontRightSwerveAngle = Math.atan2(B,C)*180/Math.PI;
        backRightSwerveSpeed =  Math.sqrt(Math.pow(B,2.0) + Math.pow(D,2.0));
        backRightSwerveAngle = Math.atan2(B,D)*180/Math.PI;
        
        //SET ALL OF THE NUMBERS FOR THE SWERVE VARS
      }
      public void setVector(double angle, double mag, double rotationVectorX){
        double angleVX = Math.cos((angle-gyroAngle)*Math.PI/180) *180/Math.PI * mag;//TODO CHECK about RAD VS DEG
        double angleVY = Math.sin((angle-gyroAngle)*Math.PI/180) *180/Math.PI * mag;
        targetAngleRate = rotationVectorX;
        setSwerve(angleVX, angleVY, angleRateVector);
      } 
      public void sanitizeAngle(){
        gyroAngle = navX.getAngle();
        while(gyroAngle > 360){
          gyroAngle = gyroAngle - 360;
        }
        while(gyroAngle < 0){
          gyroAngle = gyroAngle + 360;
        }

      }

      double xDis;
      public void xDisplacement(){
        xDis = xDis + 0.05 *navX.getVelocityX();
      }
      public void angleRatePID(double target){
        target = target * 180;//Takes in value -1 - 1 and turns it into max / min 180/-180
        angleRateVector = -1* angleRatePID.calculate(navX.getRate(), target);
        
      }
      
      public void periodic(){
        //System.out.println(frontLeftSwerveSpeed/300);
        //+180+Constants.flHome
        //System.out.println(frontLeft.getEncoder() + "FL");
        //System.out.println(frontRight.getEncoder() + "FR");
        //System.out.println(backLeft.getEncoder() + "BL");
        //System.out.println(backRight.getEncoder() + "BR");
        sanitizeAngle();
        angleRatePID(targetAngleRate);
        /*System.out.println(backRightSwerveAngle);
        System.out.println(backRightSwerveSpeed);
        System.out.println(backLeftSwerveAngle);
        System.out.println(backLeftSwerveSpeed);
        System.out.println(frontRightSwerveAngle);
        System.out.println(frontRightSwerveSpeed);
        System.out.println(frontLeftSwerveAngle);
        System.out.println(frontLeftSwerveSpeed);*/
          // Get my gyro angle. We are negating the value because gyros return positive
         // values as the robot turns clockwise. This is not standard convention that is
        // used by the WPILib classes.
        // Update the pose


        frontLeft.setSpeed(frontLeftSwerveSpeed/75, frontLeft.anglePIDCalcABS(frontLeftSwerveAngle+180));
        frontRight.setSpeed(frontRightSwerveSpeed/75, frontRight.anglePIDCalcABS(frontRightSwerveAngle+180));
        backLeft.setSpeed(backLeftSwerveSpeed/75, backLeft.anglePIDCalcABS(backLeftSwerveAngle+180));
        backRight.setSpeed(backRightSwerveSpeed/75, backRight.anglePIDCalcABS(backRightSwerveAngle+180));
        System.out.println(navX.getAngle() + "raw");
        System.out.println(gyroAngle);
      }


}