package de.aw.awlib.calculator;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by alex on 11.03.2016.
 */
public class CalculatorBrain {
    // operator types
    public static final String ADD = "+";
    public static final String SUBTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String CLEAR = "C";
    public static final String CLEARMEMORY = "MC";
    public static final String ADDTOMEMORY = "M+";
    public static final String SUBTRACTFROMMEMORY = "M-";
    public static final String RECALLMEMORY = "MR";
    public static final String SQUAREROOT = "√";
    public static final String SQUARED = "x²";
    public static final String INVERT = "1/x";
    public static final String TOGGLESIGN = "+/-";
    public static final String SINE = "sin";
    public static final String COSINE = "cos";
    public static final String TANGENT = "tan";
    private double mCalculatorMemory;
    // 3 + 6 = 9
    // 3 & 6 are called the operand.
    // The + is called the operator.
    // 9 is the result of the operation.
    private double mOperand;
    private double mWaitingOperand;
    private String mWaitingOperator;
    // public static final String EQUALS = "=";

    // constructor
    public CalculatorBrain() {
        // initialize variables upon start
        mOperand = 0;
        mWaitingOperand = 0;
        mWaitingOperator = "";
        mCalculatorMemory = 0;
    }

    public CalculatorBrain(Double initialValue) {
        this();
        if (initialValue != null) {
            mOperand = initialValue;
        }
    }

    // used on screen orientation change
    public double getMemory() {
        return mCalculatorMemory;
    }

    // used on screen orientation change
    public void setMemory(double calculatorMemory) {
        mCalculatorMemory = calculatorMemory;
    }

    public double getResult() {
        return mOperand;
    }

    protected double performOperation(String operator) {
        switch (operator) {
            case CLEAR:
                mOperand = 0;
                mWaitingOperator = "";
                mWaitingOperand = 0;
                // mCalculatorMemory = 0;
                break;
            case CLEARMEMORY:
                mCalculatorMemory = 0;
                break;
            case ADDTOMEMORY:
                mCalculatorMemory = mCalculatorMemory + mOperand;
                break;
            case SUBTRACTFROMMEMORY:
                mCalculatorMemory = mCalculatorMemory - mOperand;
                break;
            case RECALLMEMORY:
                mOperand = mCalculatorMemory;
                break;
            case SQUAREROOT:
                mOperand = Math.sqrt(mOperand);
                break;
            case SQUARED:
                mOperand = mOperand * mOperand;
                break;
            case INVERT:
                if (mOperand != 0) {
                    mOperand = 1 / mOperand;
                }
                break;
            case TOGGLESIGN:
                mOperand = -mOperand;
                break;
            case SINE:
                mOperand = Math.sin(Math.toRadians(
                        mOperand)); // Math.toRadians(mOperand) converts result to degrees
                break;
            case COSINE:
                mOperand = Math.cos(Math.toRadians(
                        mOperand)); // Math.toRadians(mOperand) converts result to degrees
                break;
            case TANGENT:
                mOperand = Math.tan(Math.toRadians(
                        mOperand)); // Math.toRadians(mOperand) converts result to degrees
                break;
            default:
                performWaitingOperation();
                mWaitingOperator = operator;
                mWaitingOperand = mOperand;
                break;
        }
        return mOperand;
    }

    protected void performWaitingOperation() {
        switch (mWaitingOperator) {
            case ADD:
                mOperand = mWaitingOperand + mOperand;
                break;
            case SUBTRACT:
                mOperand = mWaitingOperand - mOperand;
                break;
            case MULTIPLY:
                mOperand = mWaitingOperand * mOperand;
                break;
            case DIVIDE:
                if (mOperand != 0) {
                    mOperand = mWaitingOperand / mOperand;
                }
                break;
        }
    }

    public void setOperand(double operand) {
        mOperand = operand;
    }

    public String toString() {
        return Double.toString(mOperand);
    }
}
