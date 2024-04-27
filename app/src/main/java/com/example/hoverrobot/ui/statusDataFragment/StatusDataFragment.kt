package com.example.hoverrobot.ui.statusDataFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.hoverrobot.R
import com.example.hoverrobot.ToolBox
import com.example.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.example.hoverrobot.data.utils.StatusMapperBT
import com.example.hoverrobot.databinding.StatusDataFragmentBinding

class StatusDataFragment : Fragment() {

    private lateinit var _binding : StatusDataFragmentBinding
    private val binding get() = _binding

    private val statusDataViewModel: StatusDataViewModel by viewModels( ownerProducer = { requireActivity() } )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = StatusDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListener()
        setupObservables()
    }

    private fun setupObservables() {
        statusDataViewModel.connectionStatus.observe(viewLifecycleOwner){
            it?.let {
                binding.btnBtStatus.text = StatusMapperBT.mapStatusTostring(it,null)
                ToolBox.changeStrokeColor(requireContext(),binding.btnBtStatus, StatusMapperBT.mapStatusToColor(it,null), 2)
            }
        }

        statusDataViewModel.gralStatus.observe(viewLifecycleOwner){
            it?.let{
                binding.tvGralStatus.text = MapperGralStatus(requireContext()).mapGralStatusText(it)
                ToolBox.changeStrokeColor(requireContext(),binding.tvGralStatus, MapperGralStatus(requireContext()).mapGralStatusToColor(it), 2)
            }
        }

//        statusDataViewModel.imuStatus.observe(viewLifecycleOwner){
//            it?.let{
//                binding.tvImuStatus.text = StatusMapperCommon.statusToString(it)
//                ToolBox.changeStrokeColor(requireContext(),binding.tvImuStatus, StatusMapperCommon.statusToColor(it), 2)
//            }
//        }

        statusDataViewModel.escsTemp.observe(viewLifecycleOwner){
            it?.let{
                binding.tvEscTemp.text = getString(R.string.placehoder_temp).format(it)
                ToolBox.changeStrokeColor(requireContext(),binding.tvEscTemp, R.color.gray_80_percent, 2)
            }
        }

        statusDataViewModel.imuTemp.observe(viewLifecycleOwner){
            it?.let{
                binding.tvImuboardTemp.text = getString(R.string.placehoder_temp).format(it)
                ToolBox.changeStrokeColor(requireContext(),binding.tvImuboardTemp, R.color.gray_80_percent, 2)
            }
        }

        statusDataViewModel.batteryTemp.observe(viewLifecycleOwner){
            it?.let{
                binding.tvBatteryTemp.text = getString(R.string.placehoder_temp).format(it)
                ToolBox.changeStrokeColor(requireContext(),binding.tvBatteryTemp, R.color.gray_80_percent, 2)
            }
        }
    }

    private fun setupListener() {

        binding.btnExit.setOnClickListener {
            Navigation.findNavController(requireView()).popBackStack()
        }

        binding.btnBtStatus.setOnClickListener {

        }
    }

}