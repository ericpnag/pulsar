#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float BlendFactor;

out vec4 fragColor;

void main() {
    vec4 currTexel = texture(DiffuseSampler, texCoord);
    vec4 prevTexel = texture(PrevSampler, texCoord);

    fragColor = mix(currTexel, prevTexel, BlendFactor);
    fragColor.w = 1.0;
}
