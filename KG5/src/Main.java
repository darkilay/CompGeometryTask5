import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

import static java.lang.Math.*;

public class Main extends Application {
    private static final double RADIUS = 50; // радиус одного круга
    private static final double MOVE_DURATION_MS = 1200; // время выполнения одного перемещения
    private static final double PAUSE_DURATION_MS = 500; // пауза между перемещениями
    private static final int[] size = {700, 700};

    @Override
    public void start(Stage stage) throws Exception {

        Group root = new Group(); // корнь
        stage.setTitle("Main");
        stage.setScene(new Scene(root, size[0], size[1], Color.BLACK));
        stage.setResizable(false); // запрещаем изменять размер окна

        Group circles = new Group(); // круги
        ArrayList<SequentialTransition> animations = new ArrayList<>(); // вся анимация

        // сколько кругов нужно
        int circlesInRaw = (int) ceil(size[0] / RADIUS) + 2;
        int circlesInColumn = (int) ceil(size[1] / RADIUS) + 2;

        // создаем в два раза больше кругов (из-за движения)
        for (int x = -circlesInRaw; x <= circlesInRaw; x++) {
            for (int y = -circlesInColumn; y <= circlesInColumn; y++) {
                Circle circle = new Circle(getX(x, y), getY(x), RADIUS, Color.TRANSPARENT); // создаем прозрачный круг
                circle.setStroke(Color.WHITE); // устанавливаем белую обводку
                circle.setStrokeWidth(2); // шириной в 2 пикселя
                circles.getChildren().add(circle); // сохраняем

                if (x == 0 && y == 0) { // если это центральный круг - пропускаем, иначе создаем анимацию
                    continue;
                }

                SequentialTransition animation = new SequentialTransition(
                        // пауза
                        new PauseTransition(Duration.millis(PAUSE_DURATION_MS / 2)),
                        // перемещение
                        new PathTransition(
                                Duration.millis(MOVE_DURATION_MS),
                                new Path(
                                        new MoveTo(getX(x, y), getY(x)),
                                        // перемещаемся в точку посередине между окружностью и центром сцены
                                        new LineTo(
                                                (getX(x, y) + getX(0, 0)) / 2,
                                                (getY(x) + getY(0)) / 2
                                        )
                                ),
                                circle
                        ),
                        // пауза
                        new PauseTransition(Duration.millis(PAUSE_DURATION_MS / 2))
                );
                animation.setAutoReverse(true); // запрашиваем обратную анимацию
                animation.setCycleCount(Animation.INDEFINITE); // выполняется бесконечно
                animations.add(animation); // сохраняем
            }
        }

        root.getChildren().add(circles); // добавляем круги
        stage.show(); // запускаем сцену

        // максимальное расстояние от центра видимого после движения круга до центра сцены
        // (по отношению к нему перематываем анимацию)
        double max_distance = sqrt(pow(getX(circlesInRaw, 0) - size[0]/2, 2)
                + pow(getY(circlesInRaw) - size[1]/2, 2));

        for (SequentialTransition transition : animations) {
            PathTransition way = (PathTransition) transition.getChildren().get(1); // получаем путь
            Circle circle = (Circle) way.getNode(); // получаем круг

            double distance = sqrt(pow(circle.getCenterX() - size[0]/2, 2) + pow(circle.getCenterY() - size[1]/2, 2));
            double phase = distance / max_distance; // вычисляем % выполнения анимации

            transition.jumpTo(Duration.millis(PAUSE_DURATION_MS / 2 + MOVE_DURATION_MS * phase)); // перематываем анимацию
        }

        animations.forEach(SequentialTransition::play); // запускаем анимацию
    }

    private double getX(double x, double y) {
        return size[0]/2 + x * RADIUS + y * 2 * RADIUS;
    }

    private double getY(double x) {
        return size[1]/2 + x * sqrt(3) * RADIUS;
    }
}
