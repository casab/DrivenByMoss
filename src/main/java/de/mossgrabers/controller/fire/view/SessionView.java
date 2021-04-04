// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2021
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.fire.view;

import de.mossgrabers.controller.fire.FireConfiguration;
import de.mossgrabers.controller.fire.controller.FireColorManager;
import de.mossgrabers.controller.fire.controller.FireControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.IScene;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.utils.ButtonEvent;
import de.mossgrabers.framework.view.AbstractSessionView;
import de.mossgrabers.framework.view.SessionColor;
import de.mossgrabers.framework.view.TransposeView;


/**
 * The Session view.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class SessionView extends AbstractSessionView<FireControlSurface, FireConfiguration> implements TransposeView, IFireView
{
    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public SessionView (final FireControlSurface surface, final IModel model)
    {
        super ("Session", surface, model, 4, 16, true);

        final SessionColor isRecording = new SessionColor (FireColorManager.FIRE_COLOR_RED, FireColorManager.FIRE_COLOR_DARKER_RED, false);
        final SessionColor isRecordingQueued = new SessionColor (FireColorManager.FIRE_COLOR_RED, FireColorManager.FIRE_COLOR_GRAY, true);
        final SessionColor isPlaying = new SessionColor (FireColorManager.FIRE_COLOR_GREEN, FireColorManager.FIRE_COLOR_DARK_GREEN, false);
        final SessionColor isPlayingQueued = new SessionColor (FireColorManager.FIRE_COLOR_GREEN, FireColorManager.FIRE_COLOR_GRAY, true);
        final SessionColor hasContent = new SessionColor (FireColorManager.FIRE_COLOR_ORANGE, FireColorManager.FIRE_COLOR_WHITE, false);
        final SessionColor noContent = new SessionColor (FireColorManager.FIRE_COLOR_BLACK, -1, false);
        final SessionColor recArmed = new SessionColor (FireColorManager.FIRE_COLOR_DARK_RED, -1, false);
        this.setColors (isRecording, isRecordingQueued, isPlaying, isPlayingQueued, hasContent, noContent, recArmed);

        this.birdColorHasContent = hasContent;
        this.birdColorSelected = isPlaying;
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        final int n = note;

        // Birds-eye-view navigation
        if (this.isBirdsEyeActive ())
        {
            if (velocity == 0)
                return;

            final int index = n - 36;
            final int x = index % this.columns;
            final int y = this.rows - 1 - index / this.columns;

            this.onGridNoteBirdsEyeView (x, y, 0);
            return;
        }

        super.onGridNote (n, velocity);
    }


    /** {@inheritDoc} */
    @Override
    public int getSoloButtonColor (final int index)
    {
        final ISceneBank sceneBank = this.model.getSceneBank ();
        final IScene s = sceneBank.getItem (index);
        return s.doesExist () && s.isSelected () ? 4 : 0;
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        final int scene = buttonID.ordinal () - ButtonID.SCENE1.ordinal ();
        if (scene < 0 || scene >= 4)
            return 0;

        final ISceneBank sceneBank = this.model.getSceneBank ();
        final IScene s = sceneBank.getItem (scene);
        if (!s.doesExist ())
            return 0;
        return this.surface.isPressed (buttonID) ? 2 : 1;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isBirdsEyeActive ()
    {
        return this.isBirdsEyeActive;
    }


    /** {@inheritDoc} */
    @Override
    public void onButton (final ButtonID buttonID, final ButtonEvent event, final int velocity)
    {
        if (ButtonID.isSceneButton (buttonID) && this.surface.isPressed (ButtonID.ALT))
        {
            this.model.getCurrentTrackBank ().stop ();
            return;
        }

        switch (buttonID)
        {
            case ARROW_LEFT:
                if (event == ButtonEvent.DOWN)
                    this.model.getCurrentTrackBank ().selectPreviousPage ();
                break;

            case ARROW_RIGHT:
                if (event == ButtonEvent.DOWN)
                    this.model.getCurrentTrackBank ().selectNextPage ();
                break;

            default:
                super.onButton (buttonID, event, velocity);
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    protected boolean handleButtonCombinations (final ITrack track, final ISlot slot)
    {
        final boolean result = super.handleButtonCombinations (track, slot);

        // Stop clip with normal stop button
        if (this.isButtonCombination (ButtonID.STOP))
        {
            track.stop ();
            return true;
        }

        final FireConfiguration configuration = this.surface.getConfiguration ();
        if (this.isButtonCombination (ButtonID.DELETE) && configuration.isDeleteModeActive ())
            configuration.toggleDeleteModeActive ();
        else if (this.isButtonCombination (ButtonID.DUPLICATE) && configuration.isDuplicateModeActive () && (!slot.doesExist () || !slot.hasContent ()))
            configuration.toggleDuplicateModeActive ();

        return result;
    }


    /** {@inheritDoc} */
    @Override
    protected boolean isButtonCombination (final ButtonID buttonID)
    {
        if (super.isButtonCombination (buttonID))
            return true;

        final FireConfiguration configuration = this.surface.getConfiguration ();
        if (buttonID == ButtonID.DELETE && configuration.isDeleteModeActive ())
            return true;

        return buttonID == ButtonID.DUPLICATE && configuration.isDuplicateModeActive ();
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveDown (final ButtonEvent event)
    {
        // Not used
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveUp (final ButtonEvent event)
    {
        // Not used
    }


    /** {@inheritDoc} */
    @Override
    public boolean isOctaveUpButtonOn ()
    {
        // Not used
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isOctaveDownButtonOn ()
    {
        // Not used
        return false;
    }


    /** {@inheritDoc} */
    @Override
    public void onSelectKnobValue (final int value)
    {
        final ISceneBank sceneBank = this.model.getSceneBank ();

        if (this.model.getValueChanger ().isIncrease (value))
        {
            if (this.surface.isPressed (ButtonID.SELECT))
            {
                if (sceneBank.canScrollPageForwards ())
                {
                    sceneBank.selectNextPage ();
                    return;
                }

                final int positionOfLastItem = sceneBank.getPositionOfLastItem ();
                if (positionOfLastItem >= 0)
                {
                    final int index = positionOfLastItem % sceneBank.getPageSize ();
                    final IScene lastItem = sceneBank.getItem (index);
                    if (!lastItem.isSelected ())
                        lastItem.select ();
                }
                return;
            }
            sceneBank.scrollForwards ();
            return;
        }

        if (this.surface.isPressed (ButtonID.SELECT))
        {
            if (sceneBank.canScrollPageBackwards ())
            {
                sceneBank.selectPreviousPage ();
                return;
            }

            final IScene firstItem = sceneBank.getItem (0);
            if (!firstItem.isSelected ())
                firstItem.select ();
            return;
        }

        sceneBank.scrollBackwards ();
    }


    /** {@inheritDoc} */
    @Override
    protected void launchScene (final IScene scene)
    {
        if (!scene.doesExist ())
            return;

        scene.select ();

        if (!this.surface.isPressed (ButtonID.SHIFT))
            scene.launch ();

        this.surface.getDisplay ().notify (scene.getName ());
    }
}