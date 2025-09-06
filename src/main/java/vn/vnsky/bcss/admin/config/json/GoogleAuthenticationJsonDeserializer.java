package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import vn.vnsky.bcss.admin.config.auth.GoogleAuthenticationToken;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
public class GoogleAuthenticationJsonDeserializer extends JsonDeserializer<GoogleAuthenticationToken> {

    @Override
    public GoogleAuthenticationToken deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);
        UserDTO userDTO = mapper.treeToValue(jsonNode.get("principal"), UserDTO.class);
        AppDTO appDTO = mapper.treeToValue(jsonNode.get("details"), AppDTO.class);
        GoogleAuthenticationToken googleAuthenticationToken = new GoogleAuthenticationToken(null, null);
        googleAuthenticationToken.setDetails(appDTO);
        googleAuthenticationToken.makeAuthenticated(userDTO);
        return googleAuthenticationToken;
    }
}
