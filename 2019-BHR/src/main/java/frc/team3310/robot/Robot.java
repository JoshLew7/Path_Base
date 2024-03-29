/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.team3310.robot;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team3310.auto.commands.LazyLoadCommandGroup;
import frc.team3310.robot.loops.Looper;
import frc.team3310.robot.paths.TrajectoryGenerator;
import frc.team3310.robot.paths.TrajectoryGenerator.RightLeftAutonSide;
import frc.team3310.robot.subsystems.Drive;
import frc.team3310.robot.subsystems.Drive.DriveControlMode;
import frc.team3310.robot.subsystems.RobotStateEstimator;
import frc.team3310.utility.lib.control.RobotStatus;

public class Robot extends TimedRobot {
	// public static OI oi;

	// Declare subsystems
	public static final Drive drive = Drive.getInstance();
	public static final RobotStateEstimator estimator = RobotStateEstimator.getInstance();
	public static final TrajectoryGenerator trajectoryGenerator = TrajectoryGenerator.getInstance();

	// Control looper
	public static final Looper controlLoop = new Looper();

	// Choosers
	private SendableChooser<OperationMode> operationModeChooser;
	private SendableChooser<Command> autonTaskChooser;
	private SendableChooser<RightLeftAutonSide> autonRightLeftChooser;
	private Command autonomousCommand;
	private Command previousAutonomousCommand;

	public static enum OperationMode {
		TEST, PRACTICE, COMPETITION
	};

	public static OperationMode operationMode = OperationMode.COMPETITION;

	public static RightLeftAutonSide rightLeftSide = RightLeftAutonSide.RIGHT;

	// PDP
	public static final PowerDistributionPanel pdp = new PowerDistributionPanel();

	// State
	private RobotStatus robotState = RobotStatus.getInstance();

	public Robot() {
		super(Constants.kLooperDt * 2);
		// System.out.println("Main loop period = " + getPeriod());
	}

	public void zeroAllSensors() {
		drive.zeroSensors();
	}

	// Called at the start of connection
	@Override
	public void robotInit() {
		// oi = OI.getInstance();

		controlLoop.register(drive);
		RobotStateEstimator.getInstance().registerEnabledLoops(controlLoop);
		trajectoryGenerator.generateTrajectories();

		operationModeChooser = new SendableChooser<OperationMode>();
		operationModeChooser.addOption("Practice", OperationMode.PRACTICE);
		operationModeChooser.setDefaultOption("Competition", OperationMode.COMPETITION);
		operationModeChooser.addOption("Test", OperationMode.TEST);
		SmartDashboard.putData("Operation Mode", operationModeChooser);

		autonTaskChooser = new SendableChooser<Command>();

		autonTaskChooser.setDefaultOption("None", null);

		SmartDashboard.putData("Autonomous", autonTaskChooser);

		autonRightLeftChooser = new SendableChooser<RightLeftAutonSide>();
		autonRightLeftChooser.addOption("Left", RightLeftAutonSide.LEFT);
		autonRightLeftChooser.setDefaultOption("Right", RightLeftAutonSide.RIGHT);
		SmartDashboard.putData("Auton Side", autonRightLeftChooser);

		LiveWindow.setEnabled(false);
		LiveWindow.disableAllTelemetry();

		zeroAllSensors();
	}

	// Called every loop for all modes
	public void robotPeriodic() {
		updateStatus();
	}

	// Called once when is disabled
	@Override
	public void disabledInit() {
	}

	// Called constantly when the robot is disabled
	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();

		autonomousCommand = autonTaskChooser.getSelected();
		if (autonomousCommand != previousAutonomousCommand) {
			if (autonomousCommand != null && autonomousCommand instanceof LazyLoadCommandGroup) {
				LazyLoadCommandGroup lazyLoad = (LazyLoadCommandGroup) autonomousCommand;
				// System.out.println("Activate auton");
				// double startTime = Timer.getFPGATimestamp();
				lazyLoad.activate();
				// System.out.println("Activate auton complete t = " + (Timer.getFPGATimestamp()
				// - startTime) + " sec");
				previousAutonomousCommand = autonomousCommand;
			}
		}
	}

	// Called once at the start of auto
	@Override
	public void autonomousInit() {
		controlLoop.start();
		zeroAllSensors();

		rightLeftSide = autonRightLeftChooser.getSelected();
		trajectoryGenerator.setRightLeftAutonSide(rightLeftSide);

		if (autonomousCommand != null) {
			autonomousCommand.start();
		}
	}

	// Called constantly through auton
	@Override
	public void autonomousPeriodic() {
		Scheduler.getInstance().run();

	}

	// Called once at the start of teleOp
	@Override
	public void teleopInit() {
		if (autonomousCommand != null) {
			autonomousCommand.cancel();

		}

		operationMode = operationModeChooser.getSelected();
		drive.setControlMode(DriveControlMode.JOYSTICK);

		controlLoop.start();
		drive.endGyroCalibration();

		if (operationMode == OperationMode.COMPETITION) {
			// Robot.elevator.setJoystickPID();
		}

		if (operationMode != OperationMode.COMPETITION) {
			// Scheduler.getInstance().add(new ElevatorAutoZeroSensor());
		}
	}

	// Called constantly through teleOp
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
	}

	public Alliance getAlliance() {
		return m_ds.getAlliance();
	}

	public double getMatchTime() {
		return m_ds.getMatchTime();
	}

	public void updateStatus() {
		drive.updateStatus(operationMode);
		robotState.updateStatus(operationMode);
	}
}
