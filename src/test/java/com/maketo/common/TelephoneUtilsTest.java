package com.maketo.common;

import com.maketo.common.util.TelephoneUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TelephoneUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"697000001", "6 97 00 00 01", "+237697000001", "237 697 00 00 01"})
    void normaliseTousLesFormatsAuMemeNumero(String saisi) {
        assertThat(TelephoneUtils.normaliser(saisi)).isEqualTo("697000001");
        assertThat(saisi).matches(TelephoneUtils.REGEX);
    }

    @ParameterizedTest
    @ValueSource(strings = {"597000001", "69700000", "6970000011", "abc"})
    void refuseLesNumerosInvalides(String saisi) {
        assertThat(saisi).doesNotMatch(TelephoneUtils.REGEX);
    }
}
