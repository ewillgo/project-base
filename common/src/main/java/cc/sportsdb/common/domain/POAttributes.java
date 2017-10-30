package cc.sportsdb.common.domain;

import java.io.Serializable;

public interface POAttributes<T> extends Serializable {
    /**
     * PO object to DTO object
     *
     * @return
     */
    T toDTO();
}
