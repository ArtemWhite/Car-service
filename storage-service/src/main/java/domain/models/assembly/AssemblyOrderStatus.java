package domain.models.assembly;

import lombok.Getter;

@Getter
public enum AssemblyOrderStatus {
    CREATED("Создан"),
    ASSEMBLED("Собран"),
    FAIL("Ошибка сборки");

    private final String displayName;

    AssemblyOrderStatus(String displayName) {
        this.displayName = displayName;
    }

}