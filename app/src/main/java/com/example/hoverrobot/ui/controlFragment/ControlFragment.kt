package com.example.hoverrobot.ui.controlFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.hoverrobot.data.models.comms.AxisControl
import com.example.hoverrobot.databinding.ControlFragmentBinding

class ControlFragment : Fragment() {

    private val controlViewModel : ControlViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var _binding : ControlFragmentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ControlFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.joystickRight.setFixedCenter(true); // set up auto-define center
        setupListener()
        setupObserver()
    }

    private fun setupListener(){

        binding.joystickRight.setOnMoveListener{ angle, strength ->
            val x = (binding.joystickRight.normalizedX * 2) - 100
            val y = 100 - (binding.joystickRight.normalizedY * 2)
            controlViewModel.newCoordinatesJoystick(AxisControl(x.toShort(),y.toShort()))       // ToDO: revisar casteo por BLE
        }
    }

    private fun setupObserver(){
        controlViewModel.joyVisible.observe(viewLifecycleOwner){
            it?.let{
                if(it){
                    binding.joystickRight.visibility = View.VISIBLE
                }
                else{
                    binding.joystickRight.visibility = View.GONE
                }
            }
        }
    }
}