package com.example.lunarnova

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.lunarnova.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }

        val moonCard: CardView = binding.moonCard
        val marsCard: CardView = binding.marsCard
        moonCard.transitionName = "moonCardTransition"
        marsCard.transitionName = "marsCardTransition"
        moonCard.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_moonFragment)

            // Use FragmentNavigatorExtras for shared element transition
            val extras = FragmentNavigatorExtras(moonCard to "moonCardTransition")
            findNavController().navigate(R.id.action_SecondFragment_to_moonFragment, null, null, extras)
        }

        marsCard.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_marsFragment)

            val extras = FragmentNavigatorExtras(marsCard to "marsCardTransition")
            findNavController().navigate(R.id.action_SecondFragment_to_marsFragment, null, null, extras)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}