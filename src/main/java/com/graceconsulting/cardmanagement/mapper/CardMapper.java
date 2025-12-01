package com.graceconsulting.cardmanagement.mapper;

import com.graceconsulting.cardmanagement.dto.CardRequest;
import com.graceconsulting.cardmanagement.dto.CardResponse;
import com.graceconsulting.cardmanagement.entity.Card;
import com.graceconsulting.cardmanagement.service.EncryptionService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CardMapper {

    @Autowired
    protected EncryptionService encryptionService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "cardNumberEncrypted", source = "cardNumber", qualifiedByName = "encryptCardNumber")
    @Mapping(target = "cardNumberHash", source = "cardNumber", qualifiedByName = "hashForMapping")
    public abstract Card toEntity(CardRequest request);

    public Card toEntity(String cardNumber, String batchId) {
        String normalized = normalizeCardNumber(cardNumber);
        return Card.builder()
                .cardNumberEncrypted(encryptionService.encrypt(normalized))
                .cardNumberHash(encryptionService.hash(normalized))
                .batchId(batchId)
                .build();
    }

    @Mapping(target = "maskedNumber", source = "originalCardNumber", qualifiedByName = "maskCardNumber")
    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "createdAt", source = "card.createdAt")
    public abstract CardResponse toResponse(Card card, String originalCardNumber);

    @Named("encryptCardNumber")
    protected String encryptCardNumber(String cardNumber) {
        return encryptionService.encrypt(normalizeCardNumber(cardNumber));
    }

    @Named("hashForMapping")
    protected String hashForMapping(String cardNumber) {
        return encryptionService.hash(normalizeCardNumber(cardNumber));
    }

    @Named("maskCardNumber")
    protected String maskCardNumber(String cardNumber) {
        return encryptionService.maskCardNumber(cardNumber);
    }

    public String normalizeCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s+", "").replaceAll("-", "");
    }

    public String hashCardNumber(String cardNumber) {
        return encryptionService.hash(normalizeCardNumber(cardNumber));
    }
}
