/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.BrickValues;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;
import org.catrobat.catroid.ui.fragment.SingleSeekbar;

import java.util.List;

public class PhiroMotorMoveBackwardBrick extends FormulaBrick {

	private static final long serialVersionUID = 1L;

	private String motor;
	private transient Motor motorEnum;

	private transient SingleSeekbar speedSeekbar = new SingleSeekbar(this, BrickField.PHIRO_SPEED, R.string.phiro_motor_speed);

	public enum Motor {
		MOTOR_LEFT, MOTOR_RIGHT, MOTOR_BOTH
	}

	public PhiroMotorMoveBackwardBrick() {
		addAllowedBrickField(BrickField.PHIRO_SPEED);
	}

	public PhiroMotorMoveBackwardBrick(Motor motor, int speedValue) {
		this.motorEnum = motor;
		this.motor = motorEnum.name();
		initializeBrickFields(new Formula(speedValue));
	}

	public PhiroMotorMoveBackwardBrick(String motor, Formula speedFormula) {
		if (motor != null) {
			this.motor = motor;
			readResolve();
		}

		initializeBrickFields(speedFormula);
	}

	private void initializeBrickFields(Formula speed) {
		addAllowedBrickField(BrickField.PHIRO_SPEED);
		setFormulaWithBrickField(BrickField.PHIRO_SPEED, speed);
	}

	protected Object readResolve() {
		if (motor != null) {
			motorEnum = Motor.valueOf(motor);
		}
		return this;
	}

	@Override
	public int getRequiredResources() {
		return BLUETOOTH_PHIRO | getFormulaWithBrickField(BrickField.PHIRO_SPEED).getRequiredResources();
	}

	@Override
	public View getPrototypeView(Context context) {
		View prototypeView = super.getPrototypeView(context);
		TextView textSpeed = prototypeView.findViewById(R.id.brick_phiro_motor_backward_action_speed_edit_text);
		textSpeed.setText(formatNumberForPrototypeView(BrickValues.PHIRO_SPEED));

		Spinner phiroProMotorSpinner = prototypeView.findViewById(R.id.brick_phiro_motor_backward_action_spinner);

		ArrayAdapter<CharSequence> motorAdapter = ArrayAdapter.createFromResource(context, R.array.brick_phiro_select_motor_spinner,
				android.R.layout.simple_spinner_item);
		motorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		phiroProMotorSpinner.setAdapter(motorAdapter);
		phiroProMotorSpinner.setSelection(motorEnum.ordinal());

		return prototypeView;
	}

	@Override
	public BrickBaseType clone() {
		return new PhiroMotorMoveBackwardBrick(motor,
				getFormulaWithBrickField(BrickField.PHIRO_SPEED).clone());
	}

	@Override
	public View getCustomView(Context context, int brickId, BaseAdapter baseAdapter) {
		return speedSeekbar.getView(context);
	}

	@Override
	public int getViewResource() {
		return R.layout.brick_phiro_motor_backward;
	}

	@Override
	public View getView(Context context) {
		super.getView(context);
		TextView editSpeed = view.findViewById(R.id.brick_phiro_motor_backward_action_speed_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_SPEED).setTextFieldId(R.id.brick_phiro_motor_backward_action_speed_edit_text);
		getFormulaWithBrickField(BrickField.PHIRO_SPEED).refreshTextField(view);

		editSpeed.setOnClickListener(this);

		ArrayAdapter<CharSequence> motorAdapter = ArrayAdapter.createFromResource(context, R.array.brick_phiro_select_motor_spinner,
				android.R.layout.simple_spinner_item);
		motorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner motorSpinner = view.findViewById(R.id.brick_phiro_motor_backward_action_spinner);

		motorSpinner.setAdapter(motorAdapter);
		motorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				motorEnum = Motor.values()[position];
				motor = motorEnum.name();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		if (motorEnum == null) {
			readResolve();
		}
		motorSpinner.setSelection(motorEnum.ordinal());

		return view;
	}

	@Override
	public void showFormulaEditorToEditFormula(View view) {
		if (isSpeedOnlyANumber()) {
			FormulaEditorFragment.showCustomFragment(view, this, BrickField.PHIRO_SPEED);
		} else {
			FormulaEditorFragment.showFragment(view, this, BrickField.PHIRO_SPEED);
		}
	}

	private boolean isSpeedOnlyANumber() {
		return getFormulaWithBrickField(BrickField.PHIRO_SPEED).getRoot().getElementType()
				== FormulaElement.ElementType.NUMBER;
	}

	@Override
	public List<ScriptSequenceAction> addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
		sequence.addAction(sprite.getActionFactory().createPhiroMotorMoveBackwardActionAction(sprite, motorEnum, getFormulaWithBrickField(BrickField.PHIRO_SPEED)));
		return null;
	}
}
