package com.example.hoverrobot.ui.statusDataFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.hoverrobot.BuildConfig
import com.example.hoverrobot.R
import com.example.hoverrobot.data.utils.MapperGralStatus
import com.example.hoverrobot.data.utils.ToolBox
import com.example.hoverrobot.data.utils.StatusMapperBT
import com.example.hoverrobot.data.utils.TempColorMapper
import com.example.hoverrobot.databinding.StatusDataFragmentBinding

class StatusDataFragment : Fragment() {

    private lateinit var _binding: StatusDataFragmentBinding
    private val binding get() = _binding

    private val statusDataViewModel: StatusDataViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = StatusDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAppVersion.text = getString(R.string.version_placeholder,BuildConfig.VERSION_NAME)
        setupObservables()
    }

    private fun setupObservables() {
        statusDataViewModel.connectionStatus.observe(viewLifecycleOwner) {
            it?.let {
                binding.btnBtStatus.text = StatusMapperBT.mapStatusTostring(it, null)
                ToolBox.changeStrokeColor(
                    binding.btnBtStatus,
                    requireContext().getColor(StatusMapperBT.mapStatusToColor(it, null)),
                    3
                )
            }
        }

        statusDataViewModel.gralStatus.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvGralStatus.text = MapperGralStatus(requireContext()).mapGralStatusText(it)
                ToolBox.changeStrokeColor(
                    binding.tvGralStatus,
                    requireContext().getColor(
                        MapperGralStatus(requireContext()).mapGralStatusToColor(it)
                    ),
                    3
                )
            }
        }

        statusDataViewModel.escsTemp.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvEscTemp.text = getString(R.string.placeholder_temp).format(it)
                ToolBox.changeStrokeColor(
                    binding.tvEscTemp,
                    requireContext().getColor(TempColorMapper.mapTempToColor(it)),
                    3
                )
            }
        }

        statusDataViewModel.imuTemp.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvImuboardTemp.text = getString(R.string.placeholder_temp).format(it)
                ToolBox.changeStrokeColor(
                    binding.tvImuboardTemp,
                    requireContext().getColor(TempColorMapper.mapTempToColor(it)),
                    3
                )
            }
        }

        statusDataViewModel.batteryTemp.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvBatteryTemp.text = getString(R.string.placeholder_temp).format(it)
                ToolBox.changeStrokeColor(
                    binding.tvBatteryTemp,
                    requireContext().getColor(TempColorMapper.mapTempToColor(it)),
                    3
                )
            }
        }
    }
}