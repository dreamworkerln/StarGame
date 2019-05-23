package ru.geekbrains.math;


import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

/**
 * Утилита для работы с матрицами
 */
public class MatrixUtils {

    private MatrixUtils() {
    }

    /**
     * Расчёт матрицы перехода 4x4
     * @param mat итоговая матрица преобразований
     * @param src исходный квадрат
     * @param dst итоговый квадрат
     */
    public static void calcTransitionMatrix(Matrix4 mat, Rect src, Rect dst) {
        float scaleX = dst.getWidth() / src.getWidth();
        float scaleY = dst.getHeight() / src.getHeight();
        // scale first!
        mat.idt().scale(scaleX, scaleY, 1f).translate(dst.pos.x - src.pos.x, dst.pos.y - src.pos.y, 0f);
    }


    public static void calcTransitionMatrix(Matrix3 mat, Rect src, Rect dst) {
        float scaleX = dst.getWidth() / src.getWidth();
        float scaleY = dst.getHeight() / src.getHeight();
        // scale first!
        mat.idt().scale(scaleX, scaleY).translate(dst.pos.x - src.pos.x, dst.pos.y - src.pos.y);
    }
}
