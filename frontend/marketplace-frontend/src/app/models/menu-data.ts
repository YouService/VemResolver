export interface MenuCategory {
  id: string;
  label: string;
  icon: string; // Mantemos como fallback
  image: string; // Nova propriedade para a imagem
  subcategories: MenuSubcategoryGroup[];
}

export interface MenuSubcategoryGroup {
  title: string;
  items: string[];
}

export const MENU_DATA: MenuCategory[] = [
  {
    id: 'assistencia',
    label: 'Assistência Técnica',
    icon: 'build',
    image: 'https://img.icons8.com/fluency/96/gears.png',
    subcategories: [
      {
        title: 'Aparelhos Eletrônicos',
        items: ['Aparelho de Som', 'Aquecedor a Gás', 'Ar Condicionado', 'Câmera', 'DVD / Blu-Ray', 'Home Theater', 'Televisão', 'Video Game']
      },
      {
        title: 'Eletrodomésticos',
        items: ['Adega Climatizada', 'Fogão e Cooktop', 'Geladeira e Freezer', 'Lava Louça', 'Máquina de Costura', 'Máquina de Lavar', 'Microondas', 'Secadora de Roupas']
      },
      {
        title: 'Informática e Telefonia',
        items: ['Cabeamento de Redes', 'Celular', 'Computador Desktop', 'Fone de Ouvido', 'Impressora', 'Notebook', 'Tablet', 'Telefone Fixo', 'Telefonia PABX']
      }
    ]
  },
  {
    id: 'aulas',
    label: 'Aulas',
    icon: 'school',
    image: 'https://img.icons8.com/fluency/96/classroom.png',
    subcategories: [
      {
        title: 'Aulas Particulares',
        items: ['Matemática', 'Português', 'Inglês', 'Espanhol', 'Física', 'Química', 'Biologia', 'História', 'Geografia']
      },
      {
        title: 'Artes e Lazer',
        items: ['Dança', 'Música', 'Canto', 'Teatro', 'Artesanato', 'Fotografia', 'Culinária']
      },
      {
        title: 'Esportes',
        items: ['Personal Trainer', 'Natação', 'Artes Marciais', 'Tênis', 'Yoga', 'Pilates']
      }
    ]
  },
  {
    id: 'autos',
    label: 'Autos',
    icon: 'directions_car',
    image: 'https://img.icons8.com/fluency/96/car.png',
    subcategories: [
      {
        title: 'Mecânica e Reparos',
        items: ['Mecânica Geral', 'Auto Elétrica', 'Funilaria e Pintura', 'Ar Condicionado Automotivo', 'Troca de Óleo', 'Borracharia']
      },
      {
        title: 'Estética Automotiva',
        items: ['Lava Rápido', 'Polimento e Cristalização', 'Higienização Interna', 'Martelinho de Ouro', 'Insulfilm']
      },
      {
        title: 'Serviços Diversos',
        items: ['Guincho', 'Chaveiro Automotivo', 'Vistoria Veicular', 'Despachante', 'Instalação de Som e Acessórios']
      }
    ]
  },
  {
    id: 'consultoria',
    label: 'Consultoria',
    icon: 'business_center',
    image: 'https://img.icons8.com/fluency/96/meeting.png',
    subcategories: [
      {
        title: 'Negócios',
        items: ['Consultoria Empresarial', 'Coaching', 'Marketing Digital', 'Recursos Humanos', 'Contabilidade', 'Direito']
      },
      {
        title: 'Pessoal',
        items: ['Consultoria de Imagem', 'Personal Organizer', 'Consultoria Financeira', 'Psicologia', 'Nutrição']
      }
    ]
  },
  {
    id: 'design',
    label: 'Design e Tecnologia',
    icon: 'computer',
    image: 'https://img.icons8.com/fluency/96/web-design.png',
    subcategories: [
      {
        title: 'Design',
        items: ['Design Gráfico', 'Web Design', 'UX/UI Design', 'Edição de Vídeo', 'Animação 2D/3D', 'Ilustração']
      },
      {
        title: 'Tecnologia',
        items: ['Desenvolvimento de Sites', 'Desenvolvimento de Apps', 'Marketing Digital', 'SEO', 'Gestão de Redes Sociais']
      }
    ]
  },
  {
    id: 'eventos',
    label: 'Eventos',
    icon: 'celebration',
    image: 'https://img.icons8.com/fluency/96/party-baloons.png',
    subcategories: [
      {
        title: 'Festas',
        items: ['Buffet', 'Decoração', 'Bolo e Doces', 'DJ e Som', 'Fotografia e Filmagem', 'Animação de Festas', 'Bartender']
      },
      {
        title: 'Eventos Corporativos',
        items: ['Coffee Break', 'Recepcionista', 'Segurança', 'Limpeza Pós-Evento', 'Palestrante']
      }
    ]
  },
  {
    id: 'moda',
    label: 'Moda e Beleza',
    icon: 'checkroom',
    image: 'https://img.icons8.com/fluency/96/clothes.png',
    subcategories: [
      {
        title: 'Beleza',
        items: ['Cabeleireiro', 'Manicure e Pedicure', 'Maquiagem', 'Depilação', 'Estética Facial', 'Massagem']
      },
      {
        title: 'Moda',
        items: ['Corte e Costura', 'Consultoria de Estilo', 'Modelagem', 'Sapateiro']
      }
    ]
  },
  {
    id: 'reformas',
    label: 'Reformas e Reparos',
    icon: 'handyman',
    image: 'https://img.icons8.com/fluency/96/maintenance.png',
    subcategories: [
      {
        title: 'Construção',
        items: ['Pedreiro', 'Arquiteto', 'Engenheiro Civil', 'Empreiteiro', 'Terraplanagem']
      },
      {
        title: 'Acabamento',
        items: ['Pintor', 'Gesseiro', 'Encanador', 'Eletricista', 'Vidraceiro', 'Serralheiro', 'Marceneiro', 'Tapeceiro']
      },
      {
        title: 'Reparos',
        items: ['Marido de Aluguel', 'Desentupidora', 'Dedetizadora', 'Chaveiro', 'Instalador de Papel de Parede']
      }
    ]
  },
  {
    id: 'saude',
    label: 'Saúde',
    icon: 'favorite',
    image: 'https://img.icons8.com/fluency/96/heart-with-pulse.png',
    subcategories: [
      {
        title: 'Bem Estar',
        items: ['Psicólogo', 'Nutricionista', 'Fisioterapeuta', 'Fonoaudiólogo', 'Terapeuta Ocupacional', 'Cuidador de Idosos']
      },
      {
        title: 'Alternativa',
        items: ['Acupuntura', 'Quiropraxia', 'Reiki', 'Shiatsu']
      }
    ]
  },
  {
    id: 'domesticos',
    label: 'Serviços Domésticos',
    icon: 'home',
    image: 'https://img.icons8.com/fluency/96/housekeeping.png',
    subcategories: [
      {
        title: 'Limpeza',
        items: ['Diarista', 'Limpeza Pós-Obra', 'Limpeza de Estofados', 'Limpeza de Piscina', 'Passadeira']
      },
      {
        title: 'Culinária',
        items: ['Cozinheira', 'Churrasqueiro']
      },
      {
        title: 'Outros',
        items: ['Babá', 'Passeador de Cães', 'Adestrador de Cães', 'Pet Sitter', 'Jardineiro']
      }
    ]
  }
];