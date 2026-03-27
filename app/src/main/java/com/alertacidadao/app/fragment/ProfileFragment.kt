package com.alertacidadao.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alertacidadao.app.data.ReportRepository
import com.alertacidadao.app.data.AuthRepository
import com.alertacidadao.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStats()
        binding.switchNotifications.isChecked = true
        populateUserInfo()
    }

    private fun populateUserInfo() {
        val ctx = requireContext()
        val email = AuthRepository.getEmail(ctx)
        val savedName = AuthRepository.getName(ctx)
        val savedNeighborhood = AuthRepository.getNeighborhood(ctx)

        email?.let { binding.textViewEmail.text = "✉ $it" }

        val nameToShow = savedName?.takeIf { it.isNotBlank() } ?: email?.let { deriveDisplayName(it) } ?: "Usuário"
        binding.textViewName.text = nameToShow
        binding.textViewAvatarInitials.text = initialsFromName(nameToShow)

        // preferir bairro salvo; se não houver, usar último relato
        if (!savedNeighborhood.isNullOrBlank()) {
            binding.textViewLocation.text = savedNeighborhood
        } else {
            val latestReport = ReportRepository.getAll(ctx).firstOrNull()
            latestReport?.let {
                if (it.bairro.isNotBlank()) binding.textViewLocation.text = it.bairro
            }
        }
    }

    private fun deriveDisplayName(email: String): String {
        val prefix = email.substringBefore('@')
        val parts = prefix.split('.', '_', '-')
            .filter { it.isNotBlank() }
            .map { it.replaceFirstChar { c -> c.uppercaseChar() } }
        return if (parts.isEmpty()) prefix else parts.joinToString(" ")
    }

    private fun initialsFromName(name: String): String {
        val parts = name.split(' ').filter { it.isNotBlank() }
        return when (parts.size) {
            0 -> "?"
            1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    private fun updateStats() {
        val context = requireContext()
        val stats = ReportRepository.getStats(context)

        binding.textViewTotalCount.text = stats.total.toString()
        binding.textViewOpenCount.text = stats.open.toString()
        binding.textViewAnalysisCount.text = stats.analysis.toString()
        binding.textViewResolvedCount.text = stats.resolved.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
