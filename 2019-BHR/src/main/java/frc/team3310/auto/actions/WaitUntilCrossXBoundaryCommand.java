package frc.team3310.auto.actions;

import edu.wpi.first.wpilibj.Timer;
import frc.team3310.utility.lib.control.RobotStatus;

public class WaitUntilCrossXBoundaryCommand implements Action {

    private double mXBoundary = 0;

    public WaitUntilCrossXBoundaryCommand(double x) {
        mXBoundary = x;
    }

    @Override
    public boolean isFinished() {
        return RobotStatus.getInstance().getFieldToVehicle(Timer.getFPGATimestamp()).getTranslation().x() > mXBoundary;
    }

    @Override
    public void update() {

    }

    @Override
    public void done() {

    }

    @Override
    public void start() {

    }
}
