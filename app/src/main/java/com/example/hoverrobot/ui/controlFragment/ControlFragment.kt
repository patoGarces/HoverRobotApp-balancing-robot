package com.example.hoverrobot.ui.controlFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hoverrobot.R
import com.example.hoverrobot.data.models.comms.DirectionControl
import com.example.hoverrobot.data.utils.ToolBox.Companion.ioScope
import com.example.hoverrobot.databinding.ControlFragmentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.marcinmoskala.arcseekbar.ProgressListener;
import java.lang.Math.round

class ControlFragment : Fragment() {

    private val controlViewModel : ControlViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding : ControlFragmentBinding
    private val binding get() = _binding

    private var directionYaw: Int = 0
    private var joyAxisX: Int = 0
    private var joyAxisY: Int = 0

    private var disableCompassSet = false

    private var jobSendYawCommand: Job? = null

    private var distanceBackward: Float = MIN_DISTANCE
    private var distanceForward: Float = MIN_DISTANCE

    private val TAG = "ControlFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ControlFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joystickThrottle.setFixedCenter(true)
        binding.joystickDirection.setFixedCenter(true)
        binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(MIN_DISTANCE)
        binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(MIN_DISTANCE)
        setupListener()
        setupObserver()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListener(){

        binding.joystickDirection.setOnMoveListener{ _, _ ->
            joyAxisY = 100 - (binding.joystickThrottle.normalizedY * 2)
            joyAxisX = (binding.joystickDirection.normalizedX * 2) -100

            controlViewModel.newCoordinatesJoystick(DirectionControl(joyAxisX.toShort(),joyAxisY.toShort(),365))
        }

        binding.joystickThrottle.setOnMoveListener{ _, _ ->
            joyAxisY = 100 - (binding.joystickThrottle.normalizedY * 2)
            joyAxisX = (binding.joystickDirection.normalizedX * 2) -100
            controlViewModel.newCoordinatesJoystick(DirectionControl(joyAxisX.toShort(),joyAxisY.toShort(),365))
        }

        binding.compassView.setOnCompassDragListener {
            disableCompassSet = true
            jobSendYawCommand?.cancel()

            directionYaw = it.roundToInt()
            jobSendYawCommand = ioScope.launch {
                delay(200)
                disableCompassSet = false
                Log.d("compassView",directionYaw.toString())
                controlViewModel.newCoordinatesJoystick(DirectionControl(joyAxisX.toShort(),joyAxisY.toShort(),directionYaw.toShort()))
            }
        }

//        binding.compassView.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    Log.d("compassView","actionDown")
//                    disableCompassSet = true
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    Log.d("compassView","actionUp")
//                    disableCompassSet = false
//                }
//            }
//            true
//        }

        binding.seekbarBackward.onProgressChangedListener =
            ProgressListener { progress ->
                distanceBackward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
                binding.btnBackward.text = getString(R.string.control_move_placeholder_backward).format(distanceBackward)
            }

        binding.seekbarForward.onProgressChangedListener =
            ProgressListener { progress ->
                distanceForward = (MIN_DISTANCE + ((progress / 100f) * (MAX_DISTANCE - MIN_DISTANCE))).round(2)
                binding.btnForward.text = getString(R.string.control_move_placeholder_forward).format(distanceForward)
            }

        binding.btnForward.setOnClickListener {
            controlViewModel.sendMoveCommand(distanceForward)
        }

        binding.btnBackward.setOnClickListener {
            controlViewModel.sendMoveCommand(distanceBackward,true)
        }
    }

    private fun setupObserver(){
        controlViewModel.joyVisible.observe(viewLifecycleOwner) {
            it?.let{
                binding.joystickThrottle.isVisible = it
                binding.joystickDirection.isVisible = it
                binding.btnForward.isVisible = it
                binding.btnBackward.isVisible = it
                binding.seekbarForward.isVisible = it
                binding.seekbarBackward.isVisible = it
            }
        }

        controlViewModel.dynamicData.observe(viewLifecycleOwner) {
            if(!disableCompassSet) binding.compassView.degrees = it.yawAngle
        }
    }

    private fun Float.round(decimals: Int = 2): Float = "%.${decimals}f".format(this).toFloat()
}

private const val MIN_DISTANCE = 0.1f
private const val MAX_DISTANCE = 1f