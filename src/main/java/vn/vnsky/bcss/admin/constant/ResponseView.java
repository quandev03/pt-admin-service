package vn.vnsky.bcss.admin.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author thanhvt
 * @created 13/04/2023 - 11:14 CH
 * @project str-auth
 * @since 1.0
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseView {

    public interface QuickSearch {
    }

    public interface Public {
    }

    public interface Internal extends Public {
    }

    public interface GroupUserSuggest {
    }

}
