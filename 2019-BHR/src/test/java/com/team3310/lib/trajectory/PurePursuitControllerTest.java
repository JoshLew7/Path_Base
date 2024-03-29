package com.team3310.lib.trajectory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import frc.team3310.utility.lib.geometry.Pose2d;
import frc.team3310.utility.lib.geometry.Rotation2d;
import frc.team3310.utility.lib.geometry.Translation2d;
import frc.team3310.utility.lib.geometry.Twist2d;
import frc.team3310.utility.lib.trajectory.DistanceView;
import frc.team3310.utility.lib.trajectory.PurePursuitController;
import frc.team3310.utility.lib.trajectory.Trajectory;

public class PurePursuitControllerTest {
    // private Pose2d kCargoSideTurnToLoadingPose = new Pose2d(new Translation2d(295.75, -60.5), Rotation2d.fromDegrees(-180.00));
    // private Pose2d kLoadingPose = new Pose2d(new Translation2d(30.00, -130), Rotation2d.fromDegrees(-180.00));

    @Test
    public void test() {
        List<Translation2d> waypoints = Arrays.asList(new Translation2d(0.0, 0.0), new Translation2d(24.0, 0.0),
                new Translation2d(36.0, 12.0),
                new Translation2d(60.0, 12.0));

        // Create the reference trajectory (straight line motion between waypoints).
        Trajectory<Translation2d> reference_trajectory = new Trajectory<>(waypoints);
        DistanceView<Translation2d> arc_length_parameterized_trajectory = new DistanceView<>(reference_trajectory);
        PurePursuitController<Translation2d> controller = new PurePursuitController<>(
                arc_length_parameterized_trajectory, 1.0, 6.0, 0.1);

        Pose2d robot_pose = new Pose2d(waypoints.get(0), Rotation2d.identity());
        final int kMaxIter = 100;
        int i = 0;
        for (; i < kMaxIter; ++i) {
            if (controller.isDone())
                break;
            Twist2d steering_command = controller.steer(robot_pose);
            steering_command = steering_command.scaled(1.0 / Math.max(1.0, steering_command.norm()));
            System.out.println("Iter: " + i + ", Pose: " + robot_pose + ", Steering Command: " + steering_command);
            robot_pose = robot_pose.transformBy(Pose2d.exp(steering_command));
        }
        assertTrue(i < kMaxIter);
    }

    // @Test
    // public void testTheta180() {
    //     List<Pose2d> waypoints = Arrays.asList(kLoadingPose, kCargoSideTurnToLoadingPose );

    //     // Create the reference trajectory (straight line motion between waypoints).
    //     Trajectory<Pose2d> reference_trajectory = new Trajectory<>(waypoints);
    //     DistanceView<Pose2d> arc_length_parameterized_trajectory = new DistanceView<>(reference_trajectory);
    //     PurePursuitController<Pose2d> controller = new PurePursuitController<>(
    //             arc_length_parameterized_trajectory, 1.0, 6.0, 0.1);

    //     Pose2d robot_pose = waypoints.get(0);
    //     final int kMaxIter = 100;
    //     int i = 0;
    //     for (; i < kMaxIter; ++i) {
    //         if (controller.isDone())
    //             break;
    //         Twist2d steering_command = controller.steer(robot_pose);
    //         steering_command = steering_command.scaled(1.0 / Math.max(1.0, steering_command.norm()));
    //         System.out.println("Iter: " + i + ", Pose: " + robot_pose + ", Steering Command: " + steering_command);
    //         robot_pose = robot_pose.transformBy(Pose2d.exp(steering_command));
    //     }
    //     assertTrue(i < kMaxIter);
    // }

}
