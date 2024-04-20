package com.example.hoverrobot.ui.bottomSheetDevicesBT

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hoverrobot.MainActivity
import com.example.hoverrobot.MainActivity.Companion.SKIP_BLUETOOTH
import com.example.hoverrobot.ui.statusBarFragment.StatusBarViewModel
import com.example.hoverrobot.data.utils.StatusEnumBT
import com.example.hoverrobot.databinding.BottomSheetDevicesFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetDevicesFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDevicesFragmentBinding? = null
    private val binding get() = _binding!!

    private val bottomSheetDevicesViewModel: BottomSheetDevicesViewModel by viewModels(ownerProducer = { requireActivity() })
    private val statusBarViewModel: StatusBarViewModel by viewModels(ownerProducer = { requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetDevicesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!SKIP_BLUETOOTH) {
            this.isCancelable = false
        }
        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.expandedOffset = 20
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.maxWidth = 800
        bottomSheetBehavior.peekHeight= 120                                                         // tamaño cuando esta collapsado

        setupObservers()
        setupListener()
    }

    private fun setupListener(){
        binding.refreshBtn.setOnClickListener {
            binding.refreshBtn.setOnClickListener{
                Log.d("bluetooth","actualizando")
                (activity as MainActivity).retryDiscover()
            }
        }
    }
    private fun setupObservers() {

        bottomSheetDevicesViewModel.deviceslist.observe(viewLifecycleOwner){
            it?.let{
                initRecyclerViewBT(it)
            }
        }

        bottomSheetDevicesViewModel.statusBtnRefresh.observe(viewLifecycleOwner){
            it?.let {
                setStatusView(it)
            }
        }

        statusBarViewModel.connectionStatus.observe(viewLifecycleOwner){
            it?.let{
                if(it == StatusEnumBT.STATUS_CONNECTED){
                    dismiss()                                                                       // una vez que estoy conectado cierro el dialog.
                }
            }
        }
    }

    private fun setStatusView(status : StatusViewBt){
        when(status){

            StatusViewBt.BT_SEARCHING ->{
                binding.refreshBtn.visibility = View.GONE
                binding.pbSearching.visibility = View.VISIBLE

            }
            StatusViewBt.BT_DISCONNECTED ->{
                binding.refreshBtn.visibility = View.VISIBLE
                binding.pbSearching.visibility = View.GONE
            }

            StatusViewBt.BT_CONNECTING -> TODO()
            StatusViewBt.BT_CONNECTED -> TODO()
        }
    }

    private fun initRecyclerViewBT( devicesList : List<BluetoothDevice>){
        binding.recyclerDevicesBT.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDevicesBT.adapter = DevicesItemAdapter(devicesList) { selectedDevice ->
            deviceBtSelected(
                selectedDevice
            )
        }
    }

    private fun deviceBtSelected(deviceSelected : BluetoothDevice){
        bottomSheetDevicesViewModel.newDeviceSelected(deviceSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

