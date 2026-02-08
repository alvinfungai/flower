package com.alvinfungai.flower.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alvinfungai.flower.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SuccessFragment : Fragment(R.layout.fragment_success) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(2000) // Show for 2 seconds
            if (isAdded) {
                // Return to home after 2 seconds: keep homeFragment top of stack
                val popped = findNavController().popBackStack(R.id.homeFragment, false)

                // If for some reason homeFragment isn't in the backstack,
                // navigate there manually.
                if (!popped) {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }
}