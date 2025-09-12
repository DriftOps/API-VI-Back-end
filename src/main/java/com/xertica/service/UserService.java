package com.xertica.service;

import com.xertica.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Service
public class UserService {

    // Calcular idade a partir de birthDate
    public int calcularIdade(User user) {
        if (user.getBirthDate() == null) return 0;
        return Period.between(user.getBirthDate(), LocalDate.now()).getYears();
    }

    // Calcular IMC
    public double calcularIMC(User user) {
        if (user.getHeight() == null || user.getWeight() == null) return 0.0;

        double alturaMetros = user.getHeight() / 100.0;
        return Math.round(user.getWeight() / (alturaMetros * alturaMetros) * 100.0) / 100.0;
    }

    // Calcular meses desde o cadastro
    public long mesesDesdeCadastro(User user) {
        if (user.getCreatedAt() == null) return 0;
        return ChronoUnit.MONTHS.between(user.getCreatedAt(), LocalDate.now());
    }

    // Verificar se hoje é aniversário
    public boolean isAniversarioHoje(User user) {
        if (user.getBirthDate() == null) return false;

        LocalDate hoje = LocalDate.now();
        return user.getBirthDate().getMonth() == hoje.getMonth() &&
               user.getBirthDate().getDayOfMonth() == hoje.getDayOfMonth();
    }

    // Mensagem personalizada para o usuário
    public String gerarMensagemPersonalizada(User user) {
        StringBuilder mensagem = new StringBuilder();

        // Mensagem de aniversário
        if (isAniversarioHoje(user)) {
            int idade = calcularIdade(user);
            mensagem.append("🎉 Feliz aniversário, ").append(user.getName())
                    .append("! Você está completando ").append(idade).append(" anos hoje!\n");
        }

        // Mensagem de acompanhamento
        long meses = mesesDesdeCadastro(user);
        if (meses >= 1) {
            mensagem.append("📆 Já se passaram ").append(meses).append(" meses desde seu cadastro. Continue firme!\n");
        }

        // Mensagem de IMC
        double imc = calcularIMC(user);
        if (imc > 0) {
            mensagem.append("💪 Seu IMC atual é: ").append(imc).append("\n");
        }

        if (mensagem.isEmpty()) {
            mensagem.append("Nenhuma notificação especial para hoje.");
        }

        return mensagem.toString().trim();
    }
}
