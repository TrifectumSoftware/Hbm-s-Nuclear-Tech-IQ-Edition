package com.hbm.render.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

public class GLShear {
	private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
	private static final Matrix4f mat = new Matrix4f();

	public static void applyShearX(float shear) {
		mat.setIdentity();
		mat.m10 = shear;
		matrix.clear();
		mat.store(matrix);
		matrix.flip();
		GL11.glMultMatrix(matrix);
	}
	public static void applyShearZ(float shear) {
		mat.setIdentity();
		mat.m12 = shear;
		matrix.clear();
		mat.store(matrix);
		matrix.flip();
		GL11.glMultMatrix(matrix);
	}
	public static void applyShearXZ(float x, float z) {
		mat.setIdentity();
		mat.m10 = x;
		mat.m12 = z;
		matrix.clear();
		mat.store(matrix);
		matrix.flip();
		GL11.glMultMatrix(matrix);
	}
}
