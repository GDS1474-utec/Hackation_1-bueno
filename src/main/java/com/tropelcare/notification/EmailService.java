package com.tropelcare.notification;

import com.tropelcare.signal.TropelSignal;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String buildSubject(TropelSignal signal) {
        return "[TROPELCARE] " + signal.getSignalType()
                + " detectada en " + signal.getTropel().getName()
                + " | Severidad " + signal.getSeverity();
    }

    public String buildBody(TropelSignal signal) {
        return """
                Hola %s,

                Tu Tropel ha emitido una señal que requiere atención.

                ----------------------------------------
                Señal ID         : #%d
                Tropel           : %s (%s)
                Tipo de señal    : %s
                Severidad        : %s
                Unidad asignada  : %s
                Acción sugerida  : %s
                Estado vital     : %s
                Nivel de energía : %d/100
                Índice de caos   : %d/100
                Etapa mutación   : %d/5
                Registrada       : %s
                ----------------------------------------

                Señal original:
                "%s"

                — TropelCare Signal Engine, Tuckersoft
                """.formatted(
                signal.getGuardian().getDisplayName(),
                signal.getId(),
                signal.getTropel().getName(),
                signal.getTropel().getSpecies(),
                signal.getSignalType(),
                signal.getSeverity(),
                signal.getAssignedUnit(),
                signal.getRecommendedAction(),
                signal.getTropel().getVitalState(),
                signal.getTropel().getEnergyLevel(),
                signal.getTropel().getChaosIndex(),
                signal.getTropel().getMutationStage(),
                signal.getCreatedAt(),
                signal.getRawContent()
        );
    }

    public void sendSignalEmail(TropelSignal signal) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(signal.getGuardian().getNotificationEmail());
        message.setSubject(buildSubject(signal));
        message.setText(buildBody(signal));
        mailSender.send(message);
    }
}
