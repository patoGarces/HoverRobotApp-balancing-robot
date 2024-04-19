package com.example.hoverrobot.ui.controlFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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

//        setupListener()       // TODO revisar dependencia
//        setupObserver()
    }

//    private fun setupListener(){
//
//        binding.joystickRight.setOnMoveListener{ angle, strength ->
//            val x = (strength * Math.cos(Math.toRadians(angle.toDouble()))).toInt()
//            val y = (strength * Math.sin(Math.toRadians(angle.toDouble()))).toInt()
//
//            controlViewModel.newCoordinatesJoystick(AxisControl(x,y))
//        }
//    }
//
//    private fun setupObserver(){
//        controlViewModel.joyVisible.observe(viewLifecycleOwner){
//            it?.let{
//                if(it){
//                    binding.joystickRight.visibility = View.VISIBLE
//                }
//                else{
//                    binding.joystickRight.visibility = View.GONE
//                }
//            }
//        }
//    }
}