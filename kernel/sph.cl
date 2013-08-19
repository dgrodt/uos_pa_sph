kernel void CalcNewP(
global float* body_P,
global float* body_rho,
const float rho,
const float m,
const float c,
const float gamma
)
{
	uint id = get_gloabal_id(0);
	body_P[id] = (pow(body_rho[id]/rho, gamma)-1) * (rho * c * c) / gamma;
}

kernel void nBody_CalcNewV(
global float4* body_Pos,
global float4* body_V,
global float* body_P,
global float* body_rho,
const float rho,
const float m,
const float c,
const float gamma,
const float DELTA_T,
const float EPSILON_SQUARED)
{
	uint id = get_global_id(0);
	
	float pi = 3.14159;
	float h = 0.1;
	float delta_V = 0;
	float delta_rho = 0;
	
	for (int i = 0; i < N; i++) {
		float4 gradW = -exp(-dot(body_pos[id]-body_pos[i], body_pos[id]-body_pos[i])/(h*h)) / (pow(pi,3/2) * pow(h,5)) * (body_pos[id]-body_pos[i]);
		delta_V += (body_P[i]/(body_rho[i] * body_rho[i]) + body_P[i]/(body_rho[id] * body_rho[id])) * gradW;	//gravitation und viskosität
		delta_rho += dot(body_V[id] - body_V[i], gradW);
	}
	delta_V *= m * DELTA_T;
	delta_rho *= m * DELTA_T;

	body_V[id] += delta_V;
	body_rho[id] += delta_rho;	
}

