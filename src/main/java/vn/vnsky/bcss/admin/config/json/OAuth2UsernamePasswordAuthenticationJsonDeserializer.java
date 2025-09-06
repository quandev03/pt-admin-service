package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.vnsky.bcss.admin.config.auth.OAuth2UsernamePasswordAuthenticationToken;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.io.IOException;

public class OAuth2UsernamePasswordAuthenticationJsonDeserializer extends JsonDeserializer<OAuth2UsernamePasswordAuthenticationToken> {

    @Override
    public OAuth2UsernamePasswordAuthenticationToken deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);
        String clientIdentity = jsonNode.get("clientIdentity").asText();
        UserDTO userDTO = mapper.treeToValue(jsonNode.get("principal"), UserDTO.class);
        AppDTO appDTO = mapper.treeToValue(jsonNode.get("details"), AppDTO.class);
        OAuth2UsernamePasswordAuthenticationToken oAuth2UsernamePasswordAuthenticationToken = new OAuth2UsernamePasswordAuthenticationToken(null, null, clientIdentity);
        oAuth2UsernamePasswordAuthenticationToken.setDetails(appDTO);
        oAuth2UsernamePasswordAuthenticationToken.makeAuthenticated(userDTO);
        return oAuth2UsernamePasswordAuthenticationToken;
    }
}
