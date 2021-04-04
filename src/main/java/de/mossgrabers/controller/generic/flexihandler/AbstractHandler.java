// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2021
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.generic.flexihandler;

import de.mossgrabers.controller.generic.GenericFlexiConfiguration;
import de.mossgrabers.controller.generic.controller.GenericFlexiControlSurface;
import de.mossgrabers.controller.generic.flexihandler.utils.MidiValue;
import de.mossgrabers.framework.MVHelper;
import de.mossgrabers.framework.controller.valuechanger.DefaultValueChanger;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IModel;


/**
 * Abstract implementation for flexi handlers.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractHandler implements IFlexiCommandHandler
{
    protected static final int                                                      KNOB_MODE_ABSOLUTE        = 0;
    protected static final int                                                      KNOB_MODE_RELATIVE1       = 1;
    protected static final int                                                      KNOB_MODE_RELATIVE2       = 2;
    protected static final int                                                      KNOB_MODE_RELATIVE3       = 3;
    protected static final int                                                      KNOB_MODE_ABSOLUTE_TOGGLE = 4;

    protected static final int                                                      SCROLL_RATE               = 6;

    protected final IValueChanger                                                   absoluteLowResValueChanger;
    protected final IValueChanger                                                   relative2ValueChanger;
    protected final IValueChanger                                                   relative3ValueChanger;

    protected final IModel                                                          model;
    protected final MVHelper<GenericFlexiControlSurface, GenericFlexiConfiguration> mvHelper;
    protected final GenericFlexiControlSurface                                      surface;
    protected final GenericFlexiConfiguration                                       configuration;

    private int                                                                     movementCounter           = 0;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     * @param configuration The configuration
     * @param relative2ValueChanger The relative value changer variant 2
     * @param relative3ValueChanger The relative value changer variant 3
     */
    protected AbstractHandler (final IModel model, final GenericFlexiControlSurface surface, final GenericFlexiConfiguration configuration, final IValueChanger relative2ValueChanger, final IValueChanger relative3ValueChanger)
    {
        this.model = model;
        this.surface = surface;
        this.configuration = configuration;
        this.mvHelper = new MVHelper<> (model, surface);
        this.absoluteLowResValueChanger = new DefaultValueChanger (128, 1);
        this.relative2ValueChanger = relative2ValueChanger;
        this.relative3ValueChanger = relative3ValueChanger;
    }


    protected IValueChanger getAbsoluteValueChanger (final MidiValue value)
    {
        return value.isHighRes () ? this.model.getValueChanger () : this.absoluteLowResValueChanger;
    }


    protected IValueChanger getRelativeValueChanger (final int knobMode)
    {
        switch (knobMode)
        {
            default:
            case KNOB_MODE_RELATIVE1:
                return this.model.getValueChanger ();
            case KNOB_MODE_RELATIVE2:
                return this.relative2ValueChanger;
            case KNOB_MODE_RELATIVE3:
                return this.relative3ValueChanger;
        }
    }


    protected boolean isIncrease (final int knobMode, final MidiValue control)
    {
        return this.getRelativeValueChanger (knobMode).calcKnobChange (control.getValue ()) > 0;
    }


    /**
     * Return if the given knob mode is one of the absoulte ones.
     *
     * @param knobMode The knob mode to test
     * @return True if it is an absolute mode
     */
    public static boolean isAbsolute (final int knobMode)
    {
        return knobMode == KNOB_MODE_ABSOLUTE || knobMode == KNOB_MODE_ABSOLUTE_TOGGLE;
    }


    /**
     * Test if a button is pressed. Can only true for absolute modes.
     *
     * @param knobMode The knob/button mode
     * @param value The value to test
     * @return True if pressed
     */
    protected boolean isButtonPressed (final int knobMode, final MidiValue value)
    {
        return knobMode == KNOB_MODE_ABSOLUTE_TOGGLE || knobMode == KNOB_MODE_ABSOLUTE && value.isPositive ();
    }


    /**
     * Slows down knob movement. Increases the counter till the scroll rate.
     *
     * @return True if the knob movement should be executed otherwise false
     */
    protected boolean increaseKnobMovement ()
    {
        this.movementCounter++;
        if (this.movementCounter < SCROLL_RATE)
            return false;
        this.movementCounter = 0;
        return true;
    }
}
